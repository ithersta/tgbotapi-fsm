package com.ithersta.tgbotapi.fsm.builders

import com.ithersta.tgbotapi.fsm.FsmDsl
import com.ithersta.tgbotapi.fsm.entities.RoleFilter
import com.ithersta.tgbotapi.fsm.entities.StateMachine
import com.ithersta.tgbotapi.fsm.engines.repository.StateRepository
import dev.inmo.tgbotapi.extensions.utils.fromUserOrNull
import dev.inmo.tgbotapi.types.UserId
import dev.inmo.tgbotapi.types.commands.BotCommandScope
import dev.inmo.tgbotapi.types.update.abstracts.Update
import dev.inmo.tgbotapi.utils.PreviewFeature
import org.koin.core.component.KoinComponent

@FsmDsl
class StateMachineBuilder<BS : Any, BU : Any, K : Any> : KoinComponent {
    private val filters = mutableListOf<RoleFilter<BS, BU, *, K>>()

    inline fun <reified U : BU> role(noinline block: RoleFilterBuilder<BS, BU, U, K>.() -> Unit) {
        role(block) { it as? U }
    }

    fun anyRole(block: RoleFilterBuilder<BS, BU, BU, K>.() -> Unit) {
        role(block) { it }
    }

    fun <U : BU> role(block: RoleFilterBuilder<BS, BU, U, K>.() -> Unit, map: (BU) -> U?) {
        filters += RoleFilterBuilder<BS, BU, U, K>(map).apply(block).build()
    }

    fun build(
        initialState: BS,
        includeHelp: Boolean
    ): StateMachine<BS, BU, K> {
        return StateMachine(
            filters,
            includeHelp,
            initialState
        )
    }
}

inline fun <reified BS : Any, BU : Any, K : Any> stateMachine(
    initialState: BS,
    includeHelp: Boolean = false,
    block: StateMachineBuilder<BS, BU, K>.() -> Unit
) = StateMachineBuilder<BS, BU, K>()
    .apply(block)
    .build(initialState, includeHelp)

inline fun <reified BS : Any, K : Any> rolelessStateMachine(
    initialState: BS,
    includeHelp: Boolean = false,
    crossinline block: RoleFilterBuilder<BS, Unit, Unit, K>.() -> Unit
) = stateMachine(
    initialState = initialState,
    includeHelp = includeHelp
) {
    anyRole {
        block()
    }
}
