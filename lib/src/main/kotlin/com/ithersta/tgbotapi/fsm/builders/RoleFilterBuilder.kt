package com.ithersta.tgbotapi.fsm.builders

import com.ithersta.tgbotapi.fsm.FsmDsl
import com.ithersta.tgbotapi.fsm.entities.RoleFilter
import com.ithersta.tgbotapi.fsm.entities.StateFilter
import kotlin.reflect.KClass

@FsmDsl
class RoleFilterBuilder<BaseRole : Any, BaseState : Any>(
    private val predicate: (BaseRole?) -> Boolean,
    private val baseStateType: KClass<BaseState>
) {
    private val filters = mutableListOf<StateFilter<BaseState, *>>()

    inline fun <reified S : BaseState> state(noinline block: StateFilterBuilder<BaseState, S>.() -> Unit) {
        state(S::class, block)
    }

    fun anyState(block: StateFilterBuilder<BaseState, BaseState>.() -> Unit) {
        state(baseStateType, block)
    }

    fun <S : BaseState> state(type: KClass<S>, block: StateFilterBuilder<BaseState, S>.() -> Unit) {
        filters += StateFilterBuilder<BaseState, S>(type).apply(block).build()
    }

    fun build(): RoleFilter<BaseRole, BaseState> {
        return RoleFilter(predicate, filters)
    }
}
