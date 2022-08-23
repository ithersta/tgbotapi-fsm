package com.ithersta.tgbotapi.fsm.entities

import com.ithersta.tgbotapi.fsm.entities.triggers.AppliedHandler
import com.ithersta.tgbotapi.fsm.entities.triggers.AppliedOnStateChangedHandler
import com.ithersta.tgbotapi.fsm.repository.StateRepository
import com.ithersta.tgbotapi.fsm.tryHandlingHelp
import dev.inmo.micro_utils.coroutines.subscribeSafelyWithoutExceptionsAsync
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.createSubContextAndDoWithUpdatesFilter
import dev.inmo.tgbotapi.extensions.utils.shortcuts.executeAsync
import dev.inmo.tgbotapi.requests.bot.SetMyCommands
import dev.inmo.tgbotapi.types.BotCommand
import dev.inmo.tgbotapi.types.commands.BotCommandScope
import dev.inmo.tgbotapi.types.update.abstracts.Update

class StateMachine<BaseRole : Any, BaseState : Any, Key : Any>(
    private val filters: List<RoleFilter<BaseRole, BaseState, Key>>,
    private val includeHelp: Boolean,
    private val getKey: (Update) -> Key?,
    private val getRole: (Key) -> BaseRole?,
    private val getScope: (Key) -> BotCommandScope,
    private val stateRepository: StateRepository<Key, BaseState>
) {
    fun BehaviourContext.collect() {
        allUpdatesFlow.subscribeSafelyWithoutExceptionsAsync(scope, { getKey(it) }) { update ->
            val key = getKey(update) ?: return@subscribeSafelyWithoutExceptionsAsync
            val role = getRole(key)
            val state = stateRepository.get(key)
            createSubContextAndDoWithUpdatesFilter(stopOnCompletion = false) {
                if (includeHelp && tryHandlingHelp(update) { commands(role, state) }) {
                    return@createSubContextAndDoWithUpdatesFilter
                }
                handler(update, role, state)?.invoke(bot) { onStateChanged(key, it) }
            }
        }
    }

    private suspend fun BehaviourContext.onStateChanged(key: Key, state: BaseState) {
        val role = getRole(key)
        stateRepository.set(key, state)
        @Suppress("DeferredResultUnused")
        executeAsync(
            SetMyCommands(commands(role, state), getScope(key))
        )
        onStateChangedHandler(role, state)?.invoke(bot, key) { onStateChanged(key, state) }
    }

    private fun handler(update: Update, role: BaseRole?, state: BaseState): AppliedHandler<BaseState>? {
        return filters.firstNotNullOfOrNull { it.handler(role, update, state) }
    }

    private fun onStateChangedHandler(
        role: BaseRole?,
        state: BaseState
    ): AppliedOnStateChangedHandler<BaseState, Key>? {
        return filters.firstNotNullOfOrNull { it.onStateChangedHandler(role, state) }
    }

    private fun commands(role: BaseRole?, state: BaseState): List<BotCommand> {
        return filters.flatMap { it.commands(role, state) }
    }
}
