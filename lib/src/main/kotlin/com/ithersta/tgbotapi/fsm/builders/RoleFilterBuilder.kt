package com.ithersta.tgbotapi.fsm.builders

import com.ithersta.tgbotapi.fsm.FsmDsl
import com.ithersta.tgbotapi.fsm.entities.RoleFilter
import com.ithersta.tgbotapi.fsm.entities.StateFilter
import org.koin.core.component.KoinComponent
import kotlin.reflect.KClass

@FsmDsl
class RoleFilterBuilder<BaseRole : Any, BaseState : Any, Key : Any>(
    private val predicate: (BaseRole?) -> Boolean,
    private val baseStateType: KClass<BaseState>
) : KoinComponent {
    private val filters = mutableListOf<StateFilter<BaseState, *, Key>>()

    inline fun <reified S : BaseState> state(noinline block: StateFilterBuilder<BaseState, S, Key>.() -> Unit) {
        state(S::class, block)
    }

    fun anyState(block: StateFilterBuilder<BaseState, BaseState, Key>.() -> Unit) {
        state(baseStateType, block)
    }

    fun <S : BaseState> state(type: KClass<S>, block: StateFilterBuilder<BaseState, S, Key>.() -> Unit) {
        filters += StateFilterBuilder<BaseState, S, Key>(type).apply(block).build()
    }

    fun build(): RoleFilter<BaseRole, BaseState, Key> {
        return RoleFilter(predicate, filters)
    }
}
