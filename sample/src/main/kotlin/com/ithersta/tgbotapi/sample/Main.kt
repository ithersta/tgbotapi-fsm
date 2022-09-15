package com.ithersta.tgbotapi.sample

import com.ithersta.tgbotapi.fsm.builders.stateMachine
import com.ithersta.tgbotapi.fsm.entities.triggers.*
import com.ithersta.tgbotapi.fsm.repository.InMemoryStateRepositoryImpl
import com.ithersta.tgbotapi.menu.builders.menu
import com.ithersta.tgbotapi.pagination.PagerState
import com.ithersta.tgbotapi.pagination.statefulInlineKeyboardPager
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.send.sendTextMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.extensions.utils.types.buttons.row

sealed interface User
object Admin : User
object EmptyUser : User

val strings = (1..100).map { it.toString() }

private val stateMachine = stateMachine<DialogState, User>(
    getUser = { EmptyUser },
    stateRepository = InMemoryStateRepositoryImpl(EmptyState),
) {
    onException { userId, throwable ->
        sendTextMessage(userId, throwable.toString())
    }
    includeHelp()
    role<Admin> {
        anyState {
            onCommand("wow", "admin command") {
                sendTextMessage(it.chat, "You're an admin!")
            }
        }
    }
    role<EmptyUser> {
        state<Pager> {
            val pager = statefulInlineKeyboardPager("sample",
                onPagerStateChanged = { state.copy(pagerState = it) }
            ) { offset, limit ->
                inlineKeyboard {
                    strings.asSequence().drop(offset).take(limit).forEach {
                        row {
                            dataButton(it, TestQuery("test name $it"))
                        }
                    }
                    navigationRow(strings.size)
                }
            }
            onTransition {
                pager.sendOrEditMessage(it, "pager", state.pagerState)
            }
        }
        anyState {
            onCommand("pager", null) {
                setState(Pager(PagerState()))
            }
            onDataCallbackQuery(TestQuery::class) { (data, query) ->
                sendTextMessage(query.from, data.name)
            }
        }
        val emptyMenu = menu<DialogState, User, EmptyUser>("Меню куратора", MenuStates.Main) {
            submenu("Разослать информацию", "Выберите получателей", MenuStates.SendInfo) {
                button("Все", SendStates.ToAll)
                button("Треккеры", SendStates.ToTrackers)
                button("Выбрать отдельные команды", SendStates.ChooseTeams)
                backButton("Назад")
            }
            submenu("Получить статистику", "Какую?", MenuStates.GetStats) {
                button("Выгрузить прогресс команд", GetStatsStates.Teams, description = "Описание")
                button("Выгрузить прогресс треккеров") {
                    sendTextMessage(it.chat, "Кнопка без состояния")
                }
                backButton("Назад")
            }
            submenu("Дополнить базу пользователей", "Кого добавить?", MenuStates.AddUsers) {
                button("Добавить участников и треккеров", AddUsersStates.WaitingForDocument)
                button("Добавить куратора", AddUsersStates.CuratorDeeplink)
                backButton("Назад")
            }
            button("Выгрузить протоколы встреч", GetProtocolsState)
        }
        with(emptyMenu) { invoke() }
        println(emptyMenu.descriptions)
        state<EmptyState> {
            onTransition { sendTextMessage(it, "Empty state. You're $user") }
            onCommand("start", "register") { setState(WaitingForName) }
            onCommand("menu", "show menu") { setState(MenuStates.Main) }
        }
        state<WaitingForName> {
            onTransition {
                refreshCommands()
                sendTextMessage(it, "What's your name?")
            }
            onText { setState(WaitingForAge(it.content.text)) }
        }
        state<WaitingForAge> {
            onTransition { sendTextMessage(it, "What's your age?") }
            onText { message ->
                val age = message.content.text.toIntOrNull()?.takeIf { it > 0 } ?: run {
                    sendTextMessage(message.chat, "Invalid age")
                    return@onText
                }
                setState(WaitingForConfirmation(state.name, age))
            }
        }
        state<WaitingForConfirmation> {
            onTransition {
                sendTextMessage(it, "Confirm: ${state.name} ${state.age}. Yes/No")
            }
            onText("Yes") {
                sendTextMessage(it.chat, "Good!")
                setState(EmptyState)
            }
            onText("No") {
                sendTextMessage(it.chat, "Bad!")
                setState(EmptyState)
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
