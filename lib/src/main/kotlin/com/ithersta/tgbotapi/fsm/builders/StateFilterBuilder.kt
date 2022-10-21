package com.ithersta.tgbotapi.fsm.builders

import com.ithersta.tgbotapi.fsm.FsmDsl
import com.ithersta.tgbotapi.fsm.entities.StateFilter
import com.ithersta.tgbotapi.fsm.entities.triggers.OnStateChangedTrigger
import com.ithersta.tgbotapi.fsm.entities.triggers.Trigger
import org.koin.core.component.KoinComponent

@FsmDsl
class StateFilterBuilder<BS : Any, BU : Any, S : BS, U : BU, K : Any>(
    private val map: (BS) -> S?
) : KoinComponent {
    private val triggers = mutableListOf<Trigger<BS, BU, S, U, *>>()
    private var onStateChangedTrigger: OnStateChangedTrigger<BS, BU, S, U, K>? = null

    fun add(trigger: Trigger<BS, BU, S, U, *>) {
        triggers += trigger
    }

    fun set(trigger: OnStateChangedTrigger<BS, BU, S, U, K>) {
        check(onStateChangedTrigger == null)
        onStateChangedTrigger = trigger
    }

    fun build() = StateFilter(map, triggers, onStateChangedTrigger)
}
