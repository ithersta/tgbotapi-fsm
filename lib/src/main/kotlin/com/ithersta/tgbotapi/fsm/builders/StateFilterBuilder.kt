package com.ithersta.tgbotapi.fsm.builders

import com.ithersta.tgbotapi.fsm.FsmDsl
import com.ithersta.tgbotapi.fsm.entities.StateFilter
import com.ithersta.tgbotapi.fsm.entities.triggers.OnStateChangedTrigger
import com.ithersta.tgbotapi.fsm.entities.triggers.Trigger
import org.koin.core.component.KoinComponent
import kotlin.reflect.KClass

@FsmDsl
class StateFilterBuilder<BaseState : Any, S : BaseState, Key : Any>(
    private val type: KClass<S>
) : KoinComponent {
    private val triggers = mutableListOf<Trigger<BaseState, S, *>>()
    private var onStateChangedTrigger: OnStateChangedTrigger<BaseState, S, Key>? = null

    fun add(trigger: Trigger<BaseState, S, *>) {
        triggers += trigger
    }

    fun set(trigger: OnStateChangedTrigger<BaseState, S, Key>) {
        onStateChangedTrigger = trigger
    }

    fun build(): StateFilter<BaseState, S, Key> {
        return StateFilter(type, triggers, onStateChangedTrigger)
    }
}
