package com.ithersta.tgbotapi.test

import com.ithersta.tgbotapi.fsm.entities.StateMachine
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviour
import dev.inmo.tgbotapi.types.UserId
import dev.inmo.tgbotapi.types.update.abstracts.Update
import kotlinx.coroutines.runBlocking

class TestEngine<BS : Any, BU : Any, K : Any> internal constructor(
    private val stateMachine: StateMachine<BS, BU, K>,
    private val user: BU,
    private val key: K,
    private val bot: TelegramBot,
    initialState: BS
) {
    private val stateStackHistory = ArrayDeque(listOf(listOf(initialState)))
    val stateStack: List<BS> get() = stateStackHistory.last()
    val state: BS get() = stateStack.last()

    suspend fun dispatch(update: Update) {
        val behaviourContext = bot.buildBehaviour {}
        with(stateMachine) {
            behaviourContext.dispatch(
                update = update,
                dataAccessor = DataAccessor(
                    key = key,
                    getUser = { user },
                    getStateStackData = { stateStackHistory.last() },
                    setStateStackData = { stateStackHistory.addLast(it) },
                    rollbackStateStackData = {
                        stateStackHistory.removeLast()
                        stateStackHistory.last()
                    },
                    refreshCommands = { }
                )
            )
        }
    }
}

fun <BS : Any, BU : Any, K : Any> StateMachine<BS, BU, K>.testEngine(
    user: BU,
    key: K,
    initialState: BS,
    bot: TelegramBot = mockTelegramBot
) = TestEngine(
    stateMachine = this,
    user = user,
    key = key,
    initialState = initialState,
    bot = bot
)

fun <BS : Any, BU : Any> StateMachine<BS, BU, UserId>.testEngine(
    user: BU,
    initialState: BS,
    bot: TelegramBot = mockTelegramBot
) = testEngine(
    user = user,
    key = UserId(0L),
    initialState = initialState,
    bot = bot
)

fun <BS : Any, BU : Any> StateMachine<BS, BU, UserId>.test(
    user: BU,
    initialState: BS,
    bot: TelegramBot = mockTelegramBot,
    block: suspend TestEngine<BS, BU, UserId>.() -> Unit
) {
    runBlocking { testEngine(user, initialState, bot).block() }
}
