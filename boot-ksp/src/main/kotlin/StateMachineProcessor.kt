import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo

class StateMachineProcessor(val codeGenerator: CodeGenerator) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver
            .getSymbolsWithAnnotation("com.ithersta.tgbotapi.boot.annotations.StateMachine")
        val serializables = resolver
            .getSymbolsWithAnnotation("kotlinx.serialization.Serializable")
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.validate() }
            .toList()
        val ret = symbols.filter { !it.validate() }.toList()
        symbols
            .filter { it is KSPropertyDeclaration && it.validate() }
            .forEach { it.accept(Visitor(serializables), Unit) }
        return ret
    }

    inner class Visitor(private val serializables: Iterable<KSClassDeclaration>) : KSVisitorVoid() {
        override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit) {
            val baseQueryType = (property.annotations.first {
                it.annotationType.toTypeName() == ClassName("com.ithersta.tgbotapi.boot.annotations", "StateMachine")
            }.arguments[0].value as KSType).toTypeName()
            val typeArguments = property.getter!!.returnType!!.element!!.typeArguments
            val baseStateType = typeArguments[0].toTypeName()
            val baseUserType = typeArguments[1].toTypeName()
            val keyType = typeArguments[2].toTypeName()
            val packageName = property.containingFile!!.packageName.asString()
            generateTypeAliases(packageName, baseStateType, baseUserType, keyType)
            generateSerializersModule(packageName, baseStateType, baseQueryType)
            generateRepository(packageName, baseStateType)
            generateCallbackQueryTriggers(packageName, baseQueryType)
        }

        private fun generateCallbackQueryTriggers(packageName: String, baseQueryType: TypeName) {
            val file = FileSpec
                .scriptBuilder(packageName = packageName, fileName = "CallbackQueryTriggers")
                .addCode(
                    CodeBlock.of(
                        """import com.ithersta.tgbotapi.fsm.builders.StateFilterBuilder
import dev.inmo.tgbotapi.extensions.utils.asCallbackQueryUpdate
import dev.inmo.tgbotapi.extensions.utils.asDataCallbackQuery
import dev.inmo.tgbotapi.extensions.utils.types.buttons.InlineKeyboardRowBuilder
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.types.queries.callback.DataCallbackQuery
import dev.inmo.tgbotapi.utils.PreviewFeature
import kotlinx.serialization.*
import java.util.*
import kotlin.reflect.KClass
import com.ithersta.tgbotapi.fsm.entities.triggers.*

val Base64Encoder: Base64.Encoder = Base64.getEncoder()
val Base64Decoder: Base64.Decoder = Base64.getDecoder()
                            
@OptIn(PreviewFeature::class, ExperimentalSerializationApi::class)
inline·fun·<BS·:·Any,·BU·:·Any,·S·:·BS,·U·:·BU,·K·:·Any,·Q·:·%T>·StateFilterBuilder<BS,·BU,·S,·U,·K>.onDataCallbackQuery(
    kClass: KClass<Q>,
    crossinline filter: (Pair<Q, DataCallbackQuery>) -> Boolean = { true },
    noinline handler: Handler<BS, BU, S, U, Pair<Q, DataCallbackQuery>>
) = add(Trigger(handler) {
    asCallbackQueryUpdate()?.data?.asDataCallbackQuery()
        ?.let {
            runCatching {
                val byteArray = Base64Decoder.decode(it.data)
                protoBuf.decodeFromByteArray<%T>(byteArray) as Q to it
            }.getOrNull()
        }
        ?.takeIf(filter)
})

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified Q : %T> InlineKeyboardRowBuilder.dataButton(text: String, data: Q): Boolean {
    val byteArray = protoBuf.encodeToByteArray<%T>(data)
    return dataButton(text, Base64Encoder.encodeToString(byteArray))
}""", baseQueryType, baseQueryType, baseQueryType, baseQueryType
                    )
                )
                .build()
            file.writeTo(codeGenerator = codeGenerator, aggregating = false)
        }

        private fun generateRepository(packageName: String, baseStateType: TypeName) {
            val sqliteStateRepositoryType = ClassName("com.ithersta.tgbotapi.persistence", "SqliteStateRepository")
            val parametrizedSqliteStateRepositoryType = sqliteStateRepositoryType
                .parameterizedBy(baseStateType)
            val file = FileSpec
                .builder(packageName = packageName, fileName = "Repository")
                .addFunction(
                    FunSpec.builder("sqliteStateRepository")
                        .addParameter("historyDepth", Int::class.asTypeName())
                        .returns(parametrizedSqliteStateRepositoryType)
                        .addStatement(
                            "return %T.create(historyDepth = historyDepth, protoBuf = protoBuf)",
                            sqliteStateRepositoryType
                        )
                        .build()
                )
                .build()
            file.writeTo(codeGenerator = codeGenerator, aggregating = false)
        }

        private fun generateSerializersModule(packageName: String, baseStateType: TypeName, baseQueryType: TypeName) {
            val protobufType = ClassName("kotlinx.serialization.protobuf", "ProtoBuf")
            val file = FileSpec
                .builder(packageName = packageName, fileName = "SerializersModule")
                .addImport("kotlinx.serialization.modules", "polymorphic", "subclass")
                .addProperty(
                    PropertySpec
                        .builder("protoBuf", ClassName("kotlinx.serialization.protobuf", "ProtoBuf"))
                        .initializer("%T { serializersModule = stateMachineSerializersModule }", protobufType)
                        .build()
                )
                .addProperty(
                    PropertySpec
                        .builder(
                            "stateMachineSerializersModule",
                            ClassName("kotlinx.serialization.modules", "SerializersModule")
                        )
                        .initializer(
                            CodeBlock.builder()
                                .beginControlFlow("SerializersModule")
                                .apply {
                                    sequenceOf(baseStateType, baseQueryType).forEach { type ->
                                        beginControlFlow("polymorphic(%T::class)", type)
                                        serializables
                                            .filter { serializable -> serializable.superTypes.any { it.toTypeName() == type } }
                                            .map { it.asStarProjectedType().toTypeName() }
                                            .forEach { subclass ->
                                                addStatement("subclass(%T::class)", subclass)
                                            }
                                        endControlFlow()
                                    }
                                }
                                .endControlFlow()
                                .build()
                        )
                        .build()
                )
                .build()
            file.writeTo(codeGenerator = codeGenerator, aggregating = false)
        }

        private fun generateTypeAliases(
            packageName: String,
            baseStateType: TypeName,
            baseUserType: TypeName,
            keyType: TypeName
        ) {
            val stateTypeVariableName = TypeVariableName("State")
            val userTypeVariableName = TypeVariableName("User")
            val file = FileSpec
                .builder(packageName = packageName, fileName = "TypeAliases")
                .addTypeAlias(
                    TypeAliasSpec
                        .builder(
                            "StateMachineBuilder",
                            ClassName("com.ithersta.tgbotapi.fsm.builders", "StateMachineBuilder")
                                .parameterizedBy(baseStateType, baseUserType, keyType)
                        )
                        .build()
                )
                .addTypeAlias(
                    TypeAliasSpec
                        .builder(
                            "RoleFilterBuilder",
                            ClassName("com.ithersta.tgbotapi.fsm.builders", "RoleFilterBuilder")
                                .parameterizedBy(baseStateType, baseUserType, userTypeVariableName, keyType)
                        )
                        .addTypeVariable(userTypeVariableName)
                        .build()
                )
                .addTypeAlias(
                    TypeAliasSpec
                        .builder(
                            "StateFilterBuilder",
                            ClassName("com.ithersta.tgbotapi.fsm.builders", "StateFilterBuilder")
                                .parameterizedBy(
                                    baseStateType,
                                    baseUserType,
                                    stateTypeVariableName,
                                    userTypeVariableName,
                                    keyType
                                )
                        )
                        .addTypeVariable(stateTypeVariableName)
                        .addTypeVariable(userTypeVariableName)
                        .build()
                )
                .build()
            file.writeTo(codeGenerator = codeGenerator, aggregating = false)
        }
    }
}

class StateMachineProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return StateMachineProcessor(environment.codeGenerator)
    }
}