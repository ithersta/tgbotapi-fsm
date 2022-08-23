package com.ithersta.tgbotapi.fsm.entities

import com.ithersta.tgbotapi.fsm.entities.triggers.AppliedHandler
import com.ithersta.tgbotapi.fsm.entities.triggers.AppliedOnStateChangedHandler
import dev.inmo.tgbotapi.types.BotCommand
import dev.inmo.tgbotapi.types.update.abstracts.Update

class RoleFilter<BaseRole : Any, BaseState : Any, Key : Any>(
    private val predicate: (BaseRole?) -> Boolean,
    private val filters: List<StateFilter<BaseState, *, Key>>
) {
    fun handler(role: BaseRole?, update: Update, state: BaseState): AppliedHandler<BaseState>? {
        if (!predicate(role)) return null
        return filters.firstNotNullOfOrNull { it.handler(update, state) }
    }

    fun onStateChangedHandler(
        role: BaseRole?, state: BaseState
    ): AppliedOnStateChangedHandler<BaseState, Key>? {
        if (!predicate(role)) return null
        return filters.firstNotNullOfOrNull { it.onStateChangedHandler(state) }
    }

    fun commands(role: BaseRole?, state: BaseState): List<BotCommand> {
        if (!predicate(role)) return emptyList()
        return filters.flatMap { it.commands(state) }
    }
}
