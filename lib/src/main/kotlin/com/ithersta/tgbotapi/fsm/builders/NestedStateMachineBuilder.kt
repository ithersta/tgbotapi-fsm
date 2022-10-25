package com.ithersta.tgbotapi.fsm.builders

import com.ithersta.tgbotapi.fsm.FsmDsl
import com.ithersta.tgbotapi.fsm.entities.NestedStateMachine
import com.ithersta.tgbotapi.fsm.entities.StateFilter
import com.ithersta.tgbotapi.fsm.entities.StateMachine
import org.koin.core.component.KoinComponent

@FsmDsl
class NestedStateMachineBuilder<BS : Any, BU : Any, S : BS, U : BU, K : Any, R : Any>(
    private val level: Int,
    private val onExit: S.(R) -> BS
) : KoinComponent {
    private val filters = mutableListOf<StateFilter<BS, BU, *, U, K>>()

    inline fun <reified S : BS> state(noinline block: StateFilterBuilder<BS, BU, S, U, K>.() -> Unit) {
        state(block) { it as? S }
    }

    fun anyState(block: StateFilterBuilder<BS, BU, BS, U, K>.() -> Unit) {
        state(block) { it }
    }

    fun <S : BS> state(block: StateFilterBuilder<BS, BU, S, U, K>.() -> Unit, map: (BS) -> S?) {
        filters += StateFilterBuilder<BS, BU, S, U, K>(map, level).apply(block).build()
    }

    suspend fun exit(state: StateMachine<BS, *, *>.StateHolder<*>, result: R) {
        state.popAndOverride { onExit(it as S, result) }
    }

    fun build() = NestedStateMachine(level, filters)
}
