package com.ithersta.tgbotapi.fsm.builders

import com.ithersta.tgbotapi.fsm.FsmDsl
import com.ithersta.tgbotapi.fsm.entities.RoleFilter
import com.ithersta.tgbotapi.fsm.entities.StateMachine
import com.ithersta.tgbotapi.fsm.repository.StateRepository
import dev.inmo.tgbotapi.extensions.utils.fromUserOrNull
import dev.inmo.tgbotapi.types.UserId
import dev.inmo.tgbotapi.types.commands.BotCommandScope
import dev.inmo.tgbotapi.types.update.abstracts.Update
import dev.inmo.tgbotapi.utils.PreviewFeature
import org.koin.core.component.KoinComponent
import kotlin.reflect.KClass

@FsmDsl
class StateMachineBuilder<BaseRole : Any, BaseState : Any, Key : Any>(
    private val baseStateType: KClass<BaseState>
) : KoinComponent {
    private var includeHelp = false
    private val filters = mutableListOf<RoleFilter<BaseRole, BaseState>>()

    fun role(role: BaseRole, block: RoleFilterBuilder<BaseRole, BaseState>.() -> Unit) {
        filters += RoleFilterBuilder<BaseRole, BaseState>({ it == role }, baseStateType).apply(block).build()
    }

    fun withoutRole(block: RoleFilterBuilder<BaseRole, BaseState>.() -> Unit) {
        filters += RoleFilterBuilder<BaseRole, BaseState>({ it == null }, baseStateType).apply(block).build()
    }

    fun anyRole(block: RoleFilterBuilder<BaseRole, BaseState>.() -> Unit) {
        filters += RoleFilterBuilder<BaseRole, BaseState>({ true }, baseStateType).apply(block).build()
    }

    fun includeHelp() {
        includeHelp = true
    }

    fun build(
        getKey: (Update) -> Key?,
        getRole: (Key) -> BaseRole?,
        getScope: (Key) -> BotCommandScope,
        stateRepository: StateRepository<Key, BaseState>
    ): StateMachine<BaseRole, BaseState, Key> {
        return StateMachine(filters, includeHelp, getKey, getRole, getScope, stateRepository)
    }
}

inline fun <BaseRole : Any, reified BaseState : Any, Key : Any> stateMachine(
    noinline getKey: (Update) -> Key?,
    noinline getRole: (Key) -> BaseRole?,
    noinline getScope: (Key) -> BotCommandScope,
    stateRepository: StateRepository<Key, BaseState>,
    block: StateMachineBuilder<BaseRole, BaseState, Key>.() -> Unit
) = StateMachineBuilder<BaseRole, BaseState, Key>(BaseState::class)
    .apply(block)
    .build(getKey, getRole, getScope, stateRepository)

@OptIn(PreviewFeature::class)
inline fun <BaseRole : Any, reified BaseState : Any> stateMachine(
    noinline getRole: (UserId) -> BaseRole?,
    stateRepository: StateRepository<UserId, BaseState>,
    block: StateMachineBuilder<BaseRole, BaseState, UserId>.() -> Unit
) = stateMachine(
    getKey = { it.data.fromUserOrNull()?.from?.id },
    getScope = { BotCommandScope.Chat(it) },
    getRole = getRole,
    stateRepository = stateRepository,
    block = block
)
