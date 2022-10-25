package com.ithersta.tgbotapi.fsm.builders

import com.ithersta.tgbotapi.fsm.entities.NestedStateMachine
import com.ithersta.tgbotapi.fsm.entities.StateFilter
import org.koin.core.component.KoinComponent

class NestedStateMachineBuilder<BS : Any, BU : Any, U : BU, K : Any>(private val level: Int) : KoinComponent {
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

    fun build() = NestedStateMachine(level, filters)
}
