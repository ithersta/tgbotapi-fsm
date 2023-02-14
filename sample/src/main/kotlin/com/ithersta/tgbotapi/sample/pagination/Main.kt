package com.ithersta.tgbotapi.sample.pagination

import com.ithersta.tgbotapi.boot.annotations.StateMachine
import com.ithersta.tgbotapi.fsm.builders.rolelessStateMachine
import com.ithersta.tgbotapi.fsm.engines.regularEngine
import com.ithersta.tgbotapi.fsm.engines.repository.InMemoryStateRepositoryImpl
import com.ithersta.tgbotapi.fsm.entities.triggers.onCommand
import com.ithersta.tgbotapi.pagination.pager
import com.ithersta.tgbotapi.sample.pagination.generated.dataButton
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.send.send
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.types.UserId
import dev.inmo.tgbotapi.utils.row

@StateMachine(baseQueryKClass = Query::class)
val stateMachine = rolelessStateMachine<DialogState, UserId>(initialState = EmptyState) {
    val numbers = (0..100).toList()
    val numbersPager = pager(id = "numbers") {
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
    anyState {
        onCommand("pager", description = null) { message ->
            send(
                chat = message.chat,
                text = "Numbers",
                replyMarkup = numbersPager.replyMarkup
            )
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
