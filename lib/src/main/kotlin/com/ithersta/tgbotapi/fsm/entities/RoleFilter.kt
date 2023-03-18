package com.ithersta.tgbotapi.fsm.entities

import com.ithersta.tgbotapi.fsm.entities.triggers.AppliedHandler
import com.ithersta.tgbotapi.fsm.entities.triggers.AppliedOnStateChangedHandler
import dev.inmo.tgbotapi.types.BotCommand
import dev.inmo.tgbotapi.types.update.abstracts.Update

class RoleFilter<BS : Any, BU : Any, U : BU, K : Any>(
    private val map: (BU) -> U?,
    private val filters: List<StateFilter<BS, BU, *, U, K>>
) {
    fun handler(
        baseUser: BU,
        update: Update,
        stateHolder: StateMachine<BS, *, *>.StateHolder<BS>
    ): AppliedHandler? {
        val user = map(baseUser) ?: return null
        return filters.firstNotNullOfOrNull { it.handler(update, stateHolder, user) }
    }

    fun onStateChangedHandler(
        baseUser: BU,
        stateHolder: StateMachine<BS, *, *>.StateHolder<BS>
    ): AppliedOnStateChangedHandler<K>? {
        val user = map(baseUser) ?: return null
        return filters.firstNotNullOfOrNull { it.onStateChangedHandler(stateHolder, user) }
    }

    fun commands(baseUser: BU): List<BotCommand> {
        if (map(baseUser) == null) return emptyList()
        return filters.flatMap { it.commands() }
    }
}
