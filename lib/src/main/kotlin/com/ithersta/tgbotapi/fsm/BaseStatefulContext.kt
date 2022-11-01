package com.ithersta.tgbotapi.fsm

import com.ithersta.tgbotapi.fsm.entities.StateMachine
import dev.inmo.tgbotapi.bot.RequestsExecutor
import kotlinx.coroutines.CoroutineScope

@FsmDsl
interface BaseStatefulContext<BS : Any, BU : Any, S : BS, U : BU> : RequestsExecutor {
    val state: StateMachine<BS, *, *>.StateHolder<S>
    val refreshCommands: suspend () -> Unit
    val user: U
    val coroutineScope: CoroutineScope

    @Deprecated(
        "setState is deprecated after nested state machines introduction. Use state.override instead",
        replaceWith = ReplaceWith("state.override { value }")
    )
    suspend fun setState(value: BS) {
        state.override { value }
    }
}
