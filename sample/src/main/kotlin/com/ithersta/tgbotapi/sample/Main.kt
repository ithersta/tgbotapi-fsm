package com.ithersta.tgbotapi.sample

import com.ithersta.tgbotapi.fsm.builders.stateMachine
import com.ithersta.tgbotapi.fsm.entities.triggers.onCommand
import com.ithersta.tgbotapi.fsm.entities.triggers.onText
import com.ithersta.tgbotapi.fsm.repository.InMemoryStateRepositoryImpl
import com.ithersta.tgbotapi.sample.Role.Admin
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.send.sendTextMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling

enum class Role {
    Admin
}

private val stateMachine = stateMachine<Role, DialogState>(
    getRole = { null },
    stateRepository = InMemoryStateRepositoryImpl(EmptyState),
) {
    includeHelp()
    role(Admin) {
        anyState {
            onCommand("wow", "admin command") {
                sendTextMessage(it.chat, "You're an admin!")
            }
        }
    }
    withoutRole {
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

suspend fun main() {
    telegramBot(System.getenv("TOKEN")).buildBehaviourWithLongPolling {
        stateMachine.apply { collect() }
    }.join()
}
