package com.ithersta.tgbotapi.sample.multiplechoice

import com.ithersta.tgbotapi.boot.annotations.StateMachine
import com.ithersta.tgbotapi.fsm.builders.stateMachine
import com.ithersta.tgbotapi.fsm.engines.regularEngine
import com.ithersta.tgbotapi.fsm.engines.repository.InMemoryStateRepositoryImpl
import com.ithersta.tgbotapi.fsm.entities.triggers.onCommand
import com.ithersta.tgbotapi.fsm.entities.triggers.onEnter
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.answers.answer
import dev.inmo.tgbotapi.extensions.api.edit.reply_markup.editMessageReplyMarkup
import dev.inmo.tgbotapi.extensions.api.send.send
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.types.UserId
import dev.inmo.tgbotapi.utils.row

@StateMachine(baseQueryKClass = Query::class)
val stateMachine = stateMachine<DialogState, Unit, UserId>(
    initialState = EmptyState,
    includeHelp = true,
) {
    anyRole {
        anyState {
            onCommand("start", description = null) {
                state.override { MultipleChoiceState() }
            }
        }
        state<MultipleChoiceState> {
            onEnter { chat ->
                val keyboard = inlineKeyboard {
                    Clothes.values().forEach { clothes ->
                        row {
                            if (clothes in state.snapshot.selectedClothes) {
                                dataButton("✅${clothes.name}", UnselectQuery(clothes))
                            } else {
                                dataButton(clothes.name, SelectQuery(clothes))
                            }
                        }
                    }
                }
                state.snapshot.messageId?.let { id ->
                    runCatching {
                        editMessageReplyMarkup(chat, id, keyboard)
                    }
                } ?: run {
                    val message = send(chat, text = "Что наденем?", replyMarkup = keyboard)
                    state.overrideQuietly { copy(messageId = message.messageId) }
                }
            }
            onDataCallbackQuery(UnselectQuery::class) { (data, query) ->
                state.override { copy(selectedClothes = selectedClothes - data.clothes) }
                answer(query)
            }
            onDataCallbackQuery(SelectQuery::class) { (data, query) ->
                state.override { copy(selectedClothes = selectedClothes + data.clothes) }
                answer(query)
            }
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
