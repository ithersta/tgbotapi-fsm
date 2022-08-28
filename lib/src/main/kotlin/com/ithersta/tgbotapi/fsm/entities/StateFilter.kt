package com.ithersta.tgbotapi.fsm.entities

import com.ithersta.tgbotapi.fsm.entities.triggers.AppliedHandler
import com.ithersta.tgbotapi.fsm.entities.triggers.AppliedOnStateChangedHandler
import com.ithersta.tgbotapi.fsm.entities.triggers.OnStateChangedTrigger
import com.ithersta.tgbotapi.fsm.entities.triggers.Trigger
import dev.inmo.tgbotapi.types.BotCommand
import dev.inmo.tgbotapi.types.update.abstracts.Update

class StateFilter<BS : Any, BU : Any, S : BS, U : BU, K : Any>(
    private val map: (BS) -> S?,
    private val triggers: List<Trigger<BS, BU, S, U, *>>,
    private val onChangedTrigger: OnStateChangedTrigger<BS, BU, S, U, K>?
) {
    fun handler(update: Update, baseState: BS, user: U): AppliedHandler<BS>? {
        val state = map(baseState) ?: return null
        return triggers.firstNotNullOfOrNull { it.handler(update, state, user) }
    }

    fun onStateChangedHandler(baseState: BS, user: U): AppliedOnStateChangedHandler<BS, K>? {
        val state = map(baseState) ?: return null
        return onChangedTrigger?.handler(state, user)
    }

    fun commands(): List<BotCommand> {
        return triggers.mapNotNull { it.botCommand }
    }
}
