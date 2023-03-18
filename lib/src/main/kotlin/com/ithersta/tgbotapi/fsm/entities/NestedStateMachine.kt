package com.ithersta.tgbotapi.fsm.entities

import com.ithersta.tgbotapi.fsm.entities.triggers.AppliedHandler
import com.ithersta.tgbotapi.fsm.entities.triggers.AppliedOnStateChangedHandler
import dev.inmo.tgbotapi.types.BotCommand
import dev.inmo.tgbotapi.types.update.abstracts.Update

class NestedStateMachine<BS : Any, BU : Any, U : BU, K : Any>(
    private val level: Int,
    private val filters: List<StateFilter<BS, BU, *, U, K>>
) {
    fun handler(
        update: Update,
        stateHolder: StateMachine<BS, *, *>.StateHolder<BS>,
        user: U
    ): AppliedHandler? {
        if (stateHolder.level < level) return null
        return filters.firstNotNullOfOrNull { it.handler(update, stateHolder, user) }
    }

    fun onStateChangedHandler(
        user: U,
        stateHolder: StateMachine<BS, *, *>.StateHolder<BS>
    ): AppliedOnStateChangedHandler<K>? {
        if (stateHolder.level < level) return null
        return filters.firstNotNullOfOrNull { it.onStateChangedHandler(stateHolder, user) }
    }

    fun commands(): List<BotCommand> {
        return filters.flatMap { it.commands() }
    }
}
