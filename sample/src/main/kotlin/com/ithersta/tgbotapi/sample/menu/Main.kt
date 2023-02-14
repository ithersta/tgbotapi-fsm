package com.ithersta.tgbotapi.sample.menu

import com.ithersta.tgbotapi.boot.annotations.StateMachine
import com.ithersta.tgbotapi.fsm.builders.rolelessStateMachine
import com.ithersta.tgbotapi.fsm.engines.regularEngine
import com.ithersta.tgbotapi.fsm.engines.repository.InMemoryStateRepositoryImpl
import com.ithersta.tgbotapi.fsm.entities.triggers.onCommand
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.types.UserId

@StateMachine
val stateMachine = rolelessStateMachine<DialogState, UserId>(
    initialState = DialogState.Empty
) {
    with(sampleMenu) { invoke() }
    anyState {
        onCommand("menu", description = null) { state.override { MenuStates.Main } }
    }
}

suspend fun main() {
    telegramBot(System.getenv("TOKEN")).buildBehaviourWithLongPolling {
        stateMachine.regularEngine(
            getUser = { },
            stateRepository = InMemoryStateRepositoryImpl(historyDepth = 1),
            exceptionHandler = { _, throwable -> throwable.printStackTrace() }
        ).apply { collectUpdates() }
    }.join()
}
