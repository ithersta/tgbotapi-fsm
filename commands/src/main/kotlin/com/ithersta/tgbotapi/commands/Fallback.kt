package com.ithersta.tgbotapi.commands

import com.ithersta.tgbotapi.fsm.builders.RoleFilterBuilder
import com.ithersta.tgbotapi.fsm.builders.StateMachineBuilder
import com.ithersta.tgbotapi.fsm.entities.triggers.onText
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.api.send.sendTextMessage
import dev.inmo.tgbotapi.types.chat.Chat

fun <BS : Any> RoleFilterBuilder<BS, *, *, *>.fallback(
    action: suspend TelegramBot.(chat: Chat) -> Unit = {
        sendTextMessage(it, "Нет такой команды")
    }
) {
    anyState {
        onText { message ->
            action(message.chat)
            state.override { this }
        }
    }
}

fun <BS : Any> StateMachineBuilder<BS, *, *>.fallback(
    action: suspend TelegramBot.(chat: Chat) -> Unit = {
        sendTextMessage(it, "Нет такой команды")
    }
) {
    anyRole {
        fallback(action)
    }
}
