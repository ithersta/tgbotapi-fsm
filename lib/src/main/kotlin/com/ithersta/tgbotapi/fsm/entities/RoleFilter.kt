package com.ithersta.tgbotapi.fsm.entities

import com.ithersta.tgbotapi.fsm.entities.triggers.AppliedHandler
import dev.inmo.tgbotapi.types.BotCommand
import dev.inmo.tgbotapi.types.update.abstracts.Update

class RoleFilter<BaseRole : Any, BaseState : Any>(
    private val predicate: (BaseRole?) -> Boolean,
    private val filters: List<StateFilter<BaseState, *>>
) {
    fun handler(role: BaseRole?, update: Update, state: BaseState): AppliedHandler<BaseState>? {
        if (!predicate(role)) return null
        return filters.firstNotNullOfOrNull { it.handler(update, state) }
    }

    fun commands(role: BaseRole?, state: BaseState): List<BotCommand> {
        if (!predicate(role)) return emptyList()
        return filters.flatMap { it.commands(state) }
    }
}
