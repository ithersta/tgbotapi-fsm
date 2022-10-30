package com.ithersta.tgbotapi.fsm.entities.triggers

import com.ithersta.tgbotapi.fsm.builders.StateFilterBuilder
import dev.inmo.tgbotapi.extensions.utils.asBaseSentMessageUpdate
import dev.inmo.tgbotapi.extensions.utils.commonMessageOrNull
import dev.inmo.tgbotapi.extensions.utils.withContent
import dev.inmo.tgbotapi.types.BotCommand
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.*
import dev.inmo.tgbotapi.utils.PreviewFeature

fun <BS : Any, BU : Any, S : BS, U : BU, K : Any> StateFilterBuilder<BS, BU, S, U, K>.onText(
    vararg text: String,
    filter: (TextMessage) -> Boolean = { true },
    handler: Handler<BS, BU, S, U, TextMessage>
) = onCommonMessage(handler) { (it.content.text in text || text.isEmpty()) && filter(it) }

fun <BS : Any, BU : Any, S : BS, U : BU, K : Any> StateFilterBuilder<BS, BU, S, U, K>.onContact(
    filter: (ContactMessage) -> Boolean = { true },
    handler: Handler<BS, BU, S, U, ContactMessage>
) = onCommonMessage(handler, filter = filter)

fun <BS : Any, BU : Any, S : BS, U : BU, K : Any> StateFilterBuilder<BS, BU, S, U, K>.onDocument(
    filter: (DocumentMessage) -> Boolean = { true },
    handler: Handler<BS, BU, S, U, DocumentMessage>
) = onCommonMessage(handler, filter = filter)

fun <BS : Any, BU : Any, S : BS, U : BU, K : Any> StateFilterBuilder<BS, BU, S, U, K>.onPhoto(
    filter: (PhotoMessage) -> Boolean = { true },
    handler: Handler<BS, BU, S, U, PhotoMessage>
) = onCommonMessage(handler, filter = filter)

fun <BS : Any, BU : Any, S : BS, U : BU, K : Any> StateFilterBuilder<BS, BU, S, U, K>.onCommand(
    command: String,
    description: String?,
    filter: (TextMessage) -> Boolean = { true },
    handler: Handler<BS, BU, S, U, TextMessage>
) = onCommonMessage(
    handler,
    description?.let { BotCommand(command, it) }
) { it.content.text == "/$command" && filter(it) }

@OptIn(PreviewFeature::class)
fun <BS : Any, BU : Any, S : BS, U : BU, K : Any> StateFilterBuilder<BS, BU, S, U, K>.onDeepLink(
    handler: Handler<BS, BU, S, U, Pair<TextMessage, String>>
) = add(
    Trigger(handler) {
        val message =
            asBaseSentMessageUpdate()?.data?.commonMessageOrNull()?.withContent<TextContent>() ?: return@Trigger null
        val tokens = message.content.text.split(' ').takeIf { it.size == 2 } ?: return@Trigger null
        if (tokens.first() != "/start") return@Trigger null
        (message to tokens.last())
    }
)

@OptIn(PreviewFeature::class)
private inline fun <BS : Any, BU : Any, S : BS, U : BU, K : Any, reified T : MessageContent> StateFilterBuilder<BS, BU, S, U, K>.onCommonMessage(
    noinline handler: Handler<BS, BU, S, U, CommonMessage<T>>,
    botCommand: BotCommand? = null,
    crossinline filter: (CommonMessage<T>) -> Boolean = { true }
) = add(
    Trigger(handler, botCommand) {
        asBaseSentMessageUpdate()?.data?.commonMessageOrNull()?.withContent<T>()?.takeIf(filter)
    }
)
