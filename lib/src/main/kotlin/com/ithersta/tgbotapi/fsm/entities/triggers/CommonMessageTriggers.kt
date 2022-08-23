package com.ithersta.tgbotapi.fsm.entities.triggers

import com.ithersta.tgbotapi.fsm.builders.StateFilterBuilder
import dev.inmo.tgbotapi.extensions.utils.asBaseSentMessageUpdate
import dev.inmo.tgbotapi.extensions.utils.commonMessageOrNull
import dev.inmo.tgbotapi.extensions.utils.withContent
import dev.inmo.tgbotapi.types.BotCommand
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.ContactMessage
import dev.inmo.tgbotapi.types.message.content.DocumentMessage
import dev.inmo.tgbotapi.types.message.content.MessageContent
import dev.inmo.tgbotapi.types.message.content.TextMessage
import dev.inmo.tgbotapi.utils.PreviewFeature

fun <BaseState : Any, S : BaseState> StateFilterBuilder<BaseState, S>.onText(
    vararg text: String,
    filter: (TextMessage) -> Boolean = { true },
    handler: Handler<BaseState, S, TextMessage>
) = onCommonMessage(handler) { (it.content.text in text || text.isEmpty()) && filter(it) }

fun <BaseState : Any, S : BaseState> StateFilterBuilder<BaseState, S>.onContact(
    filter: (ContactMessage) -> Boolean = { true },
    handler: Handler<BaseState, S, ContactMessage>
) = onCommonMessage(handler, filter = filter)

fun <BaseState : Any, S : BaseState> StateFilterBuilder<BaseState, S>.onDocument(
    filter: (DocumentMessage) -> Boolean = { true },
    handler: Handler<BaseState, S, DocumentMessage>
) = onCommonMessage(handler, filter = filter)

fun <BaseState : Any, S : BaseState> StateFilterBuilder<BaseState, S>.onCommand(
    command: String,
    description: String?,
    filter: (TextMessage) -> Boolean = { true },
    handler: Handler<BaseState, S, TextMessage>
) = onCommonMessage(
    handler,
    description?.let { BotCommand(command, it) }
) { it.content.text == "/$command" && filter(it) }

@OptIn(PreviewFeature::class)
private inline fun <BaseState : Any, S : BaseState, reified T : MessageContent> StateFilterBuilder<BaseState, S>.onCommonMessage(
    noinline handler: Handler<BaseState, S, CommonMessage<T>>,
    botCommand: BotCommand? = null,
    crossinline filter: (CommonMessage<T>) -> Boolean = { true }
) = add(
    Trigger(handler, botCommand) {
        asBaseSentMessageUpdate()?.data?.commonMessageOrNull()?.withContent<T>()?.takeIf(filter)
    }
)

