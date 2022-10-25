package com.ithersta.tgbotapi.fsm.builders

import com.ithersta.tgbotapi.fsm.FsmDsl
import com.ithersta.tgbotapi.fsm.entities.NestedStateMachine
import com.ithersta.tgbotapi.fsm.entities.StateFilter
import com.ithersta.tgbotapi.fsm.entities.triggers.OnStateChangedTrigger
import com.ithersta.tgbotapi.fsm.entities.triggers.Trigger
import org.koin.core.component.KoinComponent

@FsmDsl
class StateFilterBuilder<BS : Any, BU : Any, S : BS, U : BU, K : Any>(
    private val map: (BS) -> S?,
    private val level: Int
) : KoinComponent {
    private val triggers = mutableListOf<Trigger<BS, BU, S, U, *>>()
    private val nestedStateMachines = mutableListOf<NestedStateMachine<BS, BU, U, K>>()
    private var onStateChangedTrigger: OnStateChangedTrigger<BS, BU, S, U, K>? = null

    fun add(trigger: Trigger<BS, BU, S, U, *>) {
        triggers += trigger
    }

    fun set(trigger: OnStateChangedTrigger<BS, BU, S, U, K>) {
        check(onStateChangedTrigger == null)
        onStateChangedTrigger = trigger
    }

    fun <R : Any> nestedStateMachine(
        onExit: S.(R) -> BS,
        block: NestedStateMachineBuilder<BS, BU, S, U, K, R>.() -> Unit
    ) {
        nestedStateMachines += NestedStateMachineBuilder<BS, BU, S, U, K, R>(level + 1, onExit).apply(block).build()
    }

    fun build() = StateFilter(map, triggers, nestedStateMachines, onStateChangedTrigger, level)
}
