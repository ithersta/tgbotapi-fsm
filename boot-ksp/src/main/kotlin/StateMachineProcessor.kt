import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toAnnotationSpec
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo

class StateMachineProcessor(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger
) : SymbolProcessor {
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
            verifyQueries(baseQueryType)
            val typeArguments = property.getter!!.returnType!!.element!!.typeArguments
            val baseStateType = typeArguments[0].toTypeName()
            val baseUserType = typeArguments[1].toTypeName()
            val keyType = typeArguments[2].toTypeName()
            val packageName = (property.containingFile!!.packageName.asString() + ".generated").removePrefix(".")
            generateTypeAliases(packageName, baseStateType, baseUserType, keyType)
            generateSerializersModule(packageName, baseStateType, baseQueryType)
            generateRepository(packageName, baseStateType)
            generateCallbackQueryTriggers(packageName, baseQueryType)
            generateMenuFun(packageName, baseStateType, baseUserType)
        }

        private fun generateMenuFun(packageName: String, baseStateType: TypeName, baseUserType: TypeName) {
            val userTypeVariableName = TypeVariableName("U", baseUserType)
            val originalMenuFun = MemberName("com.ithersta.tgbotapi.menu.builders", "menu")
            val menuBuilderType = ClassName("com.ithersta.tgbotapi.menu.builders", "MenuBuilder")
            FileSpec.builder(packageName, "Menu")
                .addFunction(
                    FunSpec.builder("menu")
                        .addTypeVariable(userTypeVariableName)
                        .addParameter("messageText", String::class)
                        .addParameter("state", baseStateType)
                        .addParameter(
                            "block", LambdaTypeName.get(
                                receiver = menuBuilderType.parameterizedBy(
                                    baseStateType,
                                    baseUserType,
                                    userTypeVariableName
                                ),
                                returnType = Unit::class.asTypeName()
                            )
                        )
                        .returns(
                            ClassName("com.ithersta.tgbotapi.menu.entities", "Menu").parameterizedBy(
                                baseStateType,
                                baseUserType,
                                userTypeVariableName
                            )
                        )
                        .addStatement(
                            "return %M(messageText = messageText, state = state, block = block)",
                            originalMenuFun
                        )
                        .build()
                )
                .build()
                .writeTo(codeGenerator = codeGenerator, aggregating = true)
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
import kotlin.reflect.KClass
import com.ithersta.tgbotapi.fsm.entities.triggers.*
import com.ithersta.tgbotapi.boot.encoder.Base122
                            
@OptIn(PreviewFeature::class, ExperimentalSerializationApi::class)
inline·fun·<BS·:·Any,·BU·:·Any,·S·:·BS,·U·:·BU,·K·:·Any,·reified·Q·:·%T>·StateFilterBuilder<BS,·BU,·S,·U,·K>.onDataCallbackQuery(
    kClass: KClass<Q>,
    crossinline filter: (Pair<Q, DataCallbackQuery>) -> Boolean = { true },
    noinline handler: Handler<BS, BU, S, U, Pair<Q, DataCallbackQuery>>
) = add(Trigger(handler) {
    asCallbackQueryUpdate()?.data?.asDataCallbackQuery()
        ?.let {
            runCatching {
                val byteArray = Base122.decode(it.data)
                val data = protoBuf.decodeFromByteArray<%T>(byteArray)
                if (data is Q) {
                    data to it
                } else {
                    null
                }
            }.getOrNull()
        }
        ?.takeIf(filter)
})

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified Q : %T> InlineKeyboardRowBuilder.dataButton(text: String, data: Q): Boolean {
    val byteArray = protoBuf.encodeToByteArray<%T>(data)
    val str = Base122.encode(byteArray)
    val size = str.toByteArray(Charsets.UTF_8).size
    return if (size <= 64) {
        dataButton(text, str)
    } else {
        dataButton("DATA IS TOO LARGE (" + size + " bytes > 64 bytes)", "Invalid data")
    }
}""", baseQueryType, baseQueryType, baseQueryType, baseQueryType
                    )
                )
                .build()
            file.writeTo(codeGenerator = codeGenerator, aggregating = true)
        }

        private fun generateRepository(packageName: String, baseStateType: TypeName) {
            val sqliteStateRepositoryType = ClassName("com.ithersta.tgbotapi.persistence", "SqliteStateRepository")
            val parametrizedSqliteStateRepositoryType = sqliteStateRepositoryType
                .parameterizedBy(baseStateType)
            val experimentalSerializationApi = ClassName("kotlinx.serialization", "ExperimentalSerializationApi")
            val file = FileSpec
                .scriptBuilder(packageName = packageName, fileName = "Repository")
                .addStatement("@OptIn(%T::class)", experimentalSerializationApi)
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
            file.writeTo(codeGenerator = codeGenerator, aggregating = true)
        }

        private fun verifyQueries(baseQueryType: TypeName) {
            val serialNameType = ClassName("kotlinx.serialization", "SerialName")
            serializables
                .filter { serializable ->
                    serializable.superTypes.any { it.toTypeName() == baseQueryType }
                }
                .filterNot { serializable ->
                    serializable.annotations.any { it.toAnnotationSpec().typeName == serialNameType }
                }
                .forEach { serializable ->
                    logger.error("${serializable.simpleName.asString()} is missing a required @SerialName annotation")
                }
        }

        private fun generateSerializersModule(packageName: String, baseStateType: TypeName, baseQueryType: TypeName) {
            val protobufType = ClassName("kotlinx.serialization.protobuf", "ProtoBuf")
            val experimentalSerializationApi = ClassName("kotlinx.serialization", "ExperimentalSerializationApi")
            FileSpec
                .scriptBuilder(packageName = packageName, fileName = "SerializersModule")
                .addImport("kotlinx.serialization.modules", "polymorphic", "subclass")
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
                .writeTo(codeGenerator = codeGenerator, aggregating = true)
            FileSpec
                .scriptBuilder(packageName = packageName, fileName = "ProtoBuf")
                .addStatement("@OptIn(%T::class)", experimentalSerializationApi)
                .addProperty(
                    PropertySpec
                        .builder("protoBuf", ClassName("kotlinx.serialization.protobuf", "ProtoBuf"))
                        .initializer("%T { serializersModule = stateMachineSerializersModule }", protobufType)
                        .build()
                )
                .build()
                .writeTo(codeGenerator = codeGenerator, aggregating = true)
        }

        private fun generateTypeAliases(
            packageName: String,
            baseStateType: TypeName,
            baseUserType: TypeName,
            keyType: TypeName
        ) {
            val stateTypeVariableName = TypeVariableName("S")
            val userTypeVariableName = TypeVariableName("U")
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
            file.writeTo(codeGenerator = codeGenerator, aggregating = true)
        }
    }
}

class StateMachineProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return StateMachineProcessor(environment.codeGenerator, environment.logger)
    }
}
