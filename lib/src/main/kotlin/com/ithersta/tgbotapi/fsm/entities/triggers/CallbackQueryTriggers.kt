package com.ithersta.tgbotapi.fsm.entities.triggers

import com.ithersta.tgbotapi.fsm.builders.StateFilterBuilder
import dev.inmo.tgbotapi.extensions.utils.asCallbackQueryUpdate
import dev.inmo.tgbotapi.extensions.utils.asDataCallbackQuery
import dev.inmo.tgbotapi.extensions.utils.types.buttons.InlineKeyboardRowBuilder
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.types.queries.callback.DataCallbackQuery
import dev.inmo.tgbotapi.utils.PreviewFeature
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

@OptIn(PreviewFeature::class)
fun <BS : Any, BU : Any, S : BS, U : BU, K : Any> StateFilterBuilder<BS, BU, S, U, K>.onDataCallbackQuery(
    regex: Regex,
    filter: (DataCallbackQuery) -> Boolean = { true },
    handler: Handler<BS, BU, S, U, DataCallbackQuery>
) = add(
    Trigger(handler) {
        asCallbackQueryUpdate()?.data?.asDataCallbackQuery()
            ?.takeIf(filter)
            ?.takeIf { regex.matches(it.data) }
    }
)

@OptIn(PreviewFeature::class)
inline fun <BS : Any, BU : Any, S : BS, U : BU, K : Any, reified Q : Any> StateFilterBuilder<BS, BU, S, U, K>.onDataCallbackQuery(
    kClass: KClass<Q>,
    crossinline filter: (Pair<Q, DataCallbackQuery>) -> Boolean = { true },
    noinline handler: Handler<BS, BU, S, U, Pair<Q, DataCallbackQuery>>
) = add(
    Trigger(handler) {
        val baseType = kClass.supertypes.first()
        val serializer = serializer(baseType)
        asCallbackQueryUpdate()?.data?.asDataCallbackQuery()
            ?.let { (runCatching { Json.decodeFromString(serializer, it.data) }.getOrNull() as? Q)?.to(it) }
            ?.takeIf(filter)
    }
)

inline fun <reified Q : Any> InlineKeyboardRowBuilder.dataButton(text: String, data: Q): Boolean {
    val baseType = Q::class.supertypes.first()
    val serializer = serializer(baseType)
    return dataButton(text, Json.encodeToString(serializer, data))
}
