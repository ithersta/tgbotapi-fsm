package com.ithersta.tgbotapi.sample.helloworld

import com.ithersta.tgbotapi.boot.annotations.StateMachine
import com.ithersta.tgbotapi.fsm.builders.stateMachine
import com.ithersta.tgbotapi.fsm.engines.regularEngine
import com.ithersta.tgbotapi.fsm.entities.triggers.onCommand
import com.ithersta.tgbotapi.fsm.entities.triggers.onEnter
import com.ithersta.tgbotapi.fsm.entities.triggers.onText
import com.ithersta.tgbotapi.sample.helloworld.generated.sqliteStateRepository
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.send.sendTextMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.types.UserId

@StateMachine
val stateMachine = stateMachine<DialogState, Unit, UserId>(
    initialState = EmptyState,
    includeHelp = true,
) {
    anyRole {
        anyState {
            onCommand("counter", description = "счётчик") {
                state.override { CounterState() }
            }
        }
        state<CounterState> {
            onEnter { chat ->
                sendTextMessage(chat, text = state.snapshot.number.toString())
            }
            onText("+") {
                state.override { CounterState(number + 1) }
            }
            onText("-") {
                state.override { CounterState(number - 1) }
            }
        }
    }
}

suspend fun main() {
    telegramBot(System.getenv("TOKEN")).buildBehaviourWithLongPolling {
        stateMachine.regularEngine(
            getUser = { },
            stateRepository = sqliteStateRepository(historyDepth = 3),
            exceptionHandler = { _, throwable -> throwable.printStackTrace() }
        ).apply { collectUpdates() }
    }.join()
}
