package com.ithersta.tgbotapi.fsm

import dev.inmo.tgbotapi.extensions.api.send.sendTextMessage
import kotlin.reflect.full.isSubclassOf

fun <BaseState : Any, S : BaseState> StateScope<BaseState, S>.onHelpCommand() {
    onCommand("help", null) { message ->
        val helpText = parentScope.commands
            .filterAvailable(state)
            .joinToString(separator = "\n") {
                "/${it.command} – ${it.description}"
            }
        sendTextMessage(message.chat, helpText)
    }
}

internal fun <BaseState : Any> List<CommandEntry<out BaseState>>.filterAvailable(
    state: BaseState
) = filter { state::class.isSubclassOf(it.type) }

