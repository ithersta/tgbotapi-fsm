package com.ithersta.tgbotapi.fsm.builders

import com.ithersta.tgbotapi.fsm.FsmDsl
import com.ithersta.tgbotapi.fsm.entities.StateFilter
import com.ithersta.tgbotapi.fsm.entities.triggers.Trigger
import org.koin.core.component.KoinComponent
import kotlin.reflect.KClass

@FsmDsl
class StateFilterBuilder<BaseState : Any, S : BaseState>(
    private val type: KClass<S>
) : KoinComponent {
    private val triggers = mutableListOf<Trigger<BaseState, S, *>>()

    fun add(trigger: Trigger<BaseState, S, *>) {
        triggers += trigger
    }

    fun build(): StateFilter<BaseState, S> {
        return StateFilter(type, triggers)
    }
}
