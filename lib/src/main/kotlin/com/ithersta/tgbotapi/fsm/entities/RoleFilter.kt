package com.ithersta.tgbotapi.fsm.entities

import com.ithersta.tgbotapi.fsm.entities.triggers.AppliedHandler
import com.ithersta.tgbotapi.fsm.entities.triggers.AppliedOnStateChangedHandler
import dev.inmo.tgbotapi.types.BotCommand
import dev.inmo.tgbotapi.types.update.abstracts.Update

class RoleFilter<BS : Any, BU : Any, U : BU, K : Any>(
    private val map: (BU) -> U?,
    private val filters: List<StateFilter<BS, BU, *, U, K>>
) {
    fun handler(baseUser: BU, update: Update, state: BS): AppliedHandler<BS>? {
        val user = map(baseUser) ?: return null
        return filters.firstNotNullOfOrNull { it.handler(update, state, user) }
    }

    fun onStateChangedHandler(baseUser: BU, state: BS): AppliedOnStateChangedHandler<BS, K>? {
        val user = map(baseUser) ?: return null
        return filters.firstNotNullOfOrNull { it.onStateChangedHandler(state, user) }
    }

    fun commands(baseUser: BU, state: BS): List<BotCommand> {
        if (map(baseUser) == null) return emptyList()
        return filters.flatMap { it.commands(state) }
    }
}
