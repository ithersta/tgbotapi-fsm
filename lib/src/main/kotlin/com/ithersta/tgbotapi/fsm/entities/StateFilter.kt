package com.ithersta.tgbotapi.fsm.entities

import com.ithersta.tgbotapi.fsm.entities.triggers.AppliedHandler
import com.ithersta.tgbotapi.fsm.entities.triggers.AppliedOnStateChangedHandler
import com.ithersta.tgbotapi.fsm.entities.triggers.OnStateChangedTrigger
import com.ithersta.tgbotapi.fsm.entities.triggers.Trigger
import dev.inmo.tgbotapi.types.BotCommand
import dev.inmo.tgbotapi.types.update.abstracts.Update
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.safeCast

class StateFilter<BaseState : Any, S : BaseState, Key : Any>(
    private val type: KClass<S>,
    private val triggers: List<Trigger<BaseState, S, *>>,
    private val onChangedTrigger: OnStateChangedTrigger<BaseState, S, Key>?
) {
    fun handler(update: Update, baseState: BaseState): AppliedHandler<BaseState>? {
        val state = type.safeCast(baseState) ?: return null
        return triggers.firstNotNullOfOrNull { it.handler(update, state) }
    }

    fun onStateChangedHandler(baseState: BaseState): AppliedOnStateChangedHandler<BaseState, Key>? {
        val state = type.safeCast(baseState) ?: return null
        return onChangedTrigger?.handler(state)
    }

    fun commands(baseState: BaseState): List<BotCommand> {
        if (!baseState::class.isSubclassOf(type)) return emptyList()
        return triggers.mapNotNull { it.botCommand }
    }
}
