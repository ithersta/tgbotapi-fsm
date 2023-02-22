package com.ithersta.tgbotapi.sample.statefulpagination

import com.ithersta.tgbotapi.boot.annotations.StateMachine
import com.ithersta.tgbotapi.fsm.builders.rolelessStateMachine
import com.ithersta.tgbotapi.fsm.engines.regularEngine
import com.ithersta.tgbotapi.fsm.engines.repository.InMemoryStateRepositoryImpl
import com.ithersta.tgbotapi.fsm.entities.triggers.onCommand
import com.ithersta.tgbotapi.fsm.entities.triggers.onEnter
import com.ithersta.tgbotapi.pagination.PagerState
import com.ithersta.tgbotapi.pagination.statefulPager
import com.ithersta.tgbotapi.sample.statefulpagination.generated.dataButton
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.types.UserId
import dev.inmo.tgbotapi.utils.row

@StateMachine(baseQueryKClass = Query::class)
val stateMachine = rolelessStateMachine<DialogState, UserId>(initialState = EmptyState) {
    anyState {
        onCommand("pager", description = null) {
            state.override { NumbersState(PagerState(), 0) }
        }
    }
    state<NumbersState> {
        val statefulPager = statefulPager(
            id = "numbers",
            onPagerStateChanged = { state.snapshot.copy(pagerState = it) }
        ) {
            val numbers = (state.snapshot.startWith..state.snapshot.startWith + 100).toList()
            val paginatedNumbers = numbers.drop(offset).take(limit)
            inlineKeyboard {
                paginatedNumbers.forEach { item ->
                    row {
                        dataButton(item.toString(), SampleQuery(item))
                    }
                }
                navigationRow(itemCount = numbers.size)
            }
        }
        onEnter { chatId ->
            with(statefulPager) { sendOrEditMessage(chatId, "Numbers", state.snapshot.pagerState) }
        }
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
