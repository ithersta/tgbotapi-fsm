package com.ithersta.tgbotapi.sample

import com.ithersta.tgbotapi.fsm.builders.stateMachine
import com.ithersta.tgbotapi.fsm.entities.triggers.*
import com.ithersta.tgbotapi.fsm.repository.InMemoryStateRepositoryImpl
import com.ithersta.tgbotapi.menu.builders.menu
import com.ithersta.tgbotapi.pagination.PagerState
import com.ithersta.tgbotapi.pagination.statefulInlineKeyboardPager
import com.ithersta.tgbotapi.persistence.SqliteStateRepository
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.send.sendTextMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.extensions.utils.types.buttons.row

sealed interface User
object Admin : User
object EmptyUser : User

val strings = (1..100).map { it.toString() }

private val stateMachine = stateMachine<DialogState, User>(
    getUser = { EmptyUser },
    stateRepository = InMemoryStateRepositoryImpl(),
    initialState = EmptyState
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
                onPagerStateChanged = { state.snapshot.copy(pagerState = it) }
            ) {
                inlineKeyboard {
                    strings.asSequence().drop(offset).take(limit).forEach {
                        row {
                            dataButton(it, TestQuery("test name $it"))
                        }
                    }
                    navigationRow(strings.size)
                }
            }
            onEnter {
                with(pager) { sendOrEditMessage(it, "pager", state.snapshot.pagerState) }
            }
        }
        anyState {
            onDocumentMediaGroup { message ->
                sendTextMessage(message.chat, message.content.group.size.toString())
            }
            onDocument { message ->
                sendTextMessage(message.chat, message.content.media.fileName ?: "d")
            }
            onCommand("pager", null) {
                state.override { Pager(PagerState()) }
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
            nestedStateMachine<String>(
                onExit = { WaitingForAge(it) }
            ) {
                state<WaitingForName> {
                    onEnter {
                        sendTextMessage(it, "You're in a nested state machine")
                    }
                    onText {
                        this@nestedStateMachine.exit(state, "Unit")
                    }
                }
            }
            onEnter { sendTextMessage(it, "Empty state. You're $user") }
            onCommand("start", "register") { state.override { WaitingForName } }
            onCommand("startnested", "register") { state.push(WaitingForName) }
            onCommand("menu", "show menu") { state.override { MenuStates.Main } }
        }
        state<WaitingForName> {
            onEnter {
                refreshCommands()
                sendTextMessage(it, "What's your name?")
            }
            onText { state.override { WaitingForAge(it.content.text) } }
        }
        state<WaitingForAge> {
            onEnter { sendTextMessage(it, "What's your age?") }
            onText { message ->
                val age = message.content.text.toIntOrNull()?.takeIf { it > 0 } ?: run {
                    sendTextMessage(message.chat, "Invalid age")
                    return@onText
                }
                state.override { WaitingForConfirmation(name, age) }
            }
        }
        state<WaitingForConfirmation> {
            onEnter {
                sendTextMessage(it, "Confirm: ${state.snapshot.name} ${state.snapshot.age}. Yes/No")
            }
            onText("Yes") {
                sendTextMessage(it.chat, "Good!")
                state.override { EmptyState }
            }
            onText("No") {
                sendTextMessage(it.chat, "Bad!")
                state.override { EmptyState }
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
