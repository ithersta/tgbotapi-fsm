package com.ithersta.tgbotapi.fsm

import dev.inmo.tgbotapi.extensions.utils.commonMessageOrNull
import dev.inmo.tgbotapi.extensions.utils.messageUpdateOrNull
import dev.inmo.tgbotapi.extensions.utils.withContent
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.message.content.TextMessage

fun <BaseState : Any, S : BaseState> StateScope<BaseState, S>.onText(
    vararg text: String,
    handler: Handler<BaseState, S, TextMessage>
) = on(handler) { update ->
    update.messageUpdateOrNull()?.data?.commonMessageOrNull()
        ?.withContent<TextContent>()
        ?.takeIf { it.content.text in text || text.isEmpty() }
}

fun <BaseState : Any, S : BaseState> StateScope<BaseState, S>.onCommand(
    command: String,
    handler: Handler<BaseState, S, TextMessage>
) = on(handler) { update ->
    update.messageUpdateOrNull()?.data?.commonMessageOrNull()
        ?.withContent<TextContent>()
        ?.takeIf { it.content.text == "/$command" }
}
