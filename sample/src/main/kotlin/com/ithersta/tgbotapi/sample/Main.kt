package com.ithersta.tgbotapi.sample

import com.ithersta.tgbotapi.fsm.onCommand
import com.ithersta.tgbotapi.fsm.onHelpCommand
import com.ithersta.tgbotapi.fsm.onText
import com.ithersta.tgbotapi.fsm.repository.InMemoryStateRepositoryImpl
import com.ithersta.tgbotapi.fsm.runStateMachine
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.send.sendTextMessage

suspend fun main() {
    telegramBot(System.getenv("TOKEN")).runStateMachine<DialogState>(
        repository = InMemoryStateRepositoryImpl(EmptyState)
    ) {
        state<DialogState> {
            onHelpCommand()
        }
        state<EmptyState> {
            onCommand("start", "register") {
                setState(WaitingForName)
                sendTextMessage(it.chat, "What's your name?")
            }
        }
        state<WaitingForName> {
            onText {
                val name = it.content.text
                setState(WaitingForAge(name))
                sendTextMessage(it.chat, "What's your age?")
            }
        }
        state<WaitingForAge> {
            onText { message ->
                val age = message.content.text.toIntOrNull()?.takeIf { it > 0 } ?: run {
                    sendTextMessage(message.chat, "Invalid age")
                    return@onText
                }
                setState(WaitingForConfirmation(state.name, age).also {
                    sendTextMessage(message.chat, "Confirm: ${it.name} ${it.age}. Yes/No")
                })
            }
        }
        state<WaitingForConfirmation> {
            onText("Yes") {
                setState(EmptyState)
                sendTextMessage(it.chat, "Good!")
            }
            onText("No") {
                setState(EmptyState)
                sendTextMessage(it.chat, "Bad!")
            }
            onText {
                sendTextMessage(it.chat, "Yes/No")
            }
        }
    }
}
