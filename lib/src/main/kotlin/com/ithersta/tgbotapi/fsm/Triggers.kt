package com.ithersta.tgbotapi.fsm

import dev.inmo.tgbotapi.extensions.utils.asCallbackQueryUpdate
import dev.inmo.tgbotapi.extensions.utils.asDataCallbackQuery
import dev.inmo.tgbotapi.extensions.utils.whenCommonMessage
import dev.inmo.tgbotapi.extensions.utils.withContent
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.ContactMessage
import dev.inmo.tgbotapi.types.message.content.DocumentMessage
import dev.inmo.tgbotapi.types.message.content.MessageContent
import dev.inmo.tgbotapi.types.message.content.TextMessage
import dev.inmo.tgbotapi.types.queries.callback.DataCallbackQuery
import dev.inmo.tgbotapi.types.update.abstracts.BaseSentMessageUpdate
import dev.inmo.tgbotapi.types.update.media_group.SentMediaGroupUpdate
import dev.inmo.tgbotapi.utils.PreviewFeature

fun <BaseState : Any, S : BaseState> StateScope<BaseState, S>.onText(
    vararg text: String,
    filter: (TextMessage) -> Boolean = { true },
    handler: Handler<BaseState, S, TextMessage>
) = onCommonMessage(handler) {
    (it.content.text in text || text.isEmpty()) && filter(it)
}

fun <BaseState : Any, S : BaseState> StateScope<BaseState, S>.onCommand(
    command: String,
    filter: (TextMessage) -> Boolean = { true },
    handler: Handler<BaseState, S, TextMessage>
) = onCommonMessage(handler) {
    it.content.text == "/$command" && filter(it)
}

fun <BaseState : Any, S : BaseState> StateScope<BaseState, S>.onContact(
    filter: (ContactMessage) -> Boolean = { true },
    handler: Handler<BaseState, S, ContactMessage>
) = onCommonMessage(handler, filter)

fun <BaseState : Any, S : BaseState> StateScope<BaseState, S>.onDocument(
    filter: (DocumentMessage) -> Boolean = { true },
    handler: Handler<BaseState, S, DocumentMessage>
) = onCommonMessage(handler, filter)

@OptIn(PreviewFeature::class)
fun <BaseState : Any, S : BaseState> StateScope<BaseState, S>.onDataCallbackQuery(
    regex: Regex,
    filter: (DataCallbackQuery) -> Boolean = { true },
    handler: Handler<BaseState, S, DataCallbackQuery>
) = on(handler) { update ->
    update.asCallbackQueryUpdate()?.data?.asDataCallbackQuery()
        ?.takeIf(filter)
        ?.takeIf { regex.matches(it.data) }
        ?.let(::listOfNotNull) ?: emptyList()
}

@OptIn(PreviewFeature::class)
inline fun <BaseState : Any, S : BaseState, reified T : MessageContent> StateScope<BaseState, S>.onCommonMessage(
    noinline handler: Handler<BaseState, S, CommonMessage<T>>,
    crossinline filter: (CommonMessage<T>) -> Boolean = { true }
) = on(handler) {
    when (it) {
        is BaseSentMessageUpdate -> it.data.whenCommonMessage(::listOfNotNull)
        is SentMediaGroupUpdate -> it.data
        else -> null
    }?.mapNotNull { message ->
        message.withContent<T>()?.takeIf(filter)
    } ?: emptyList()
}
