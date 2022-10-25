package com.ithersta.tgbotapi.fsm.builders

import com.ithersta.tgbotapi.fsm.FsmDsl
import com.ithersta.tgbotapi.fsm.entities.ExceptionHandler
import com.ithersta.tgbotapi.fsm.entities.RoleFilter
import com.ithersta.tgbotapi.fsm.entities.StateMachine
import com.ithersta.tgbotapi.fsm.repository.StateRepository
import dev.inmo.tgbotapi.extensions.utils.fromUserOrNull
import dev.inmo.tgbotapi.types.UserId
import dev.inmo.tgbotapi.types.commands.BotCommandScope
import dev.inmo.tgbotapi.types.update.abstracts.Update
import dev.inmo.tgbotapi.utils.PreviewFeature
import org.koin.core.component.KoinComponent

@FsmDsl
class StateMachineBuilder<BS : Any, BU : Any, K : Any> : KoinComponent {
    private var includeHelp = false
    private var exceptionHandler: ExceptionHandler<K>? = null
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

    fun includeHelp() {
        includeHelp = true
    }

    fun onException(exceptionHandler: ExceptionHandler<K>) {
        check(this.exceptionHandler == null)
        this.exceptionHandler = exceptionHandler
    }

    fun build(
        getKey: (Update) -> K?,
        getUser: (K) -> BU,
        getScope: (K) -> BotCommandScope,
        stateRepository: StateRepository<K, BS>,
        initialState: BS
    ): StateMachine<BS, BU, K> {
        return StateMachine(filters, includeHelp, getKey, getUser, getScope, stateRepository, initialState, exceptionHandler)
    }
}

inline fun <reified BS : Any, BU : Any, K : Any> stateMachine(
    noinline getKey: (Update) -> K?,
    noinline getUser: (K) -> BU,
    noinline getScope: (K) -> BotCommandScope,
    stateRepository: StateRepository<K, BS>,
    initialState: BS,
    block: StateMachineBuilder<BS, BU, K>.() -> Unit
) = StateMachineBuilder<BS, BU, K>()
    .apply(block)
    .build(getKey, getUser, getScope, stateRepository, initialState)

@OptIn(PreviewFeature::class)
inline fun <reified BS : Any, BU : Any> stateMachine(
    noinline getUser: (UserId) -> BU,
    stateRepository: StateRepository<UserId, BS>,
    initialState: BS,
    block: StateMachineBuilder<BS, BU, UserId>.() -> Unit
) = stateMachine(
    getKey = { it.data.fromUserOrNull()?.from?.id },
    getScope = { BotCommandScope.Chat(it) },
    getUser = getUser,
    stateRepository = stateRepository,
    initialState = initialState,
    block = block
)
