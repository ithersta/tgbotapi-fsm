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
    private var isFrozen = false

    fun add(trigger: Trigger<BS, BU, S, U, *>) {
        checkNotFrozen()
        triggers += trigger
    }

    fun set(trigger: OnStateChangedTrigger<BS, BU, S, U, K>) {
        checkNotFrozen()
        onStateChangedTrigger = trigger
    }

    fun build(): StateFilter<BS, BU, S, U, K> {
        isFrozen = true
        return StateFilter(map, triggers, onStateChangedTrigger)
    }

    private fun checkNotFrozen() {
        check(isFrozen.not()) {
            "Attempted to add trigger after the object was already built (check for nested triggers)"
        }
    }
}
