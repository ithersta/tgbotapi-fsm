package com.ithersta.tgbotapi.commands

import com.ithersta.tgbotapi.fsm.builders.RoleFilterBuilder
import com.ithersta.tgbotapi.fsm.builders.StateMachineBuilder
import com.ithersta.tgbotapi.fsm.entities.triggers.onCommand
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.api.send.sendTextMessage
import dev.inmo.tgbotapi.types.chat.Chat

fun <BS : Any> RoleFilterBuilder<BS, *, *, *>.cancelCommand(
    initialState: BS,
    command: String = "cancel",
    description: String = "отменить текущее действие",
    onCancellation: suspend TelegramBot.(chat: Chat) -> Unit = {
        sendTextMessage(it, "Действие отменено")
    },
    onFailure: suspend TelegramBot.(chat: Chat) -> Unit = {
        sendTextMessage(it, "Нечего отменять")
    }
) {
    anyState {
        onCommand(command, description) { message ->
            if (state.snapshot == initialState) {
                onFailure(message.chat)
            } else {
                onCancellation(message.chat)
                state.override { initialState }
            }
        }
    }
}

fun <BS : Any> StateMachineBuilder<BS, *, *>.cancelCommand(
    initialState: BS,
    command: String = "cancel",
    description: String = "отменить текущее действие",
    onCancellation: suspend TelegramBot.(chat: Chat) -> Unit = {
        sendTextMessage(it, "Действие отменено")
    },
    onFailure: suspend TelegramBot.(chat: Chat) -> Unit = {
        sendTextMessage(it, "Нечего отменять")
    }
) {
    anyRole {
        cancelCommand(initialState, command, description, onCancellation, onFailure)
    }
}
