package com.ithersta.tgbotapi.fsm.builders

import com.ithersta.tgbotapi.fsm.entities.StateFilter
import com.ithersta.tgbotapi.fsm.entities.triggers.Trigger
import kotlin.reflect.KClass

class StateFilterBuilder<BaseState : Any, S : BaseState>(private val type: KClass<S>) {
    private val triggers = mutableListOf<Trigger<BaseState, S, *>>()

    fun add(trigger: Trigger<BaseState, S, *>) {
        triggers += trigger
    }

    fun build(): StateFilter<BaseState, S> {
        return StateFilter(type, triggers)
    }
}
