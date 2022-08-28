package com.ithersta.tgbotapi.fsm.entities

import com.ithersta.tgbotapi.fsm.entities.triggers.AppliedHandler
import com.ithersta.tgbotapi.fsm.entities.triggers.AppliedOnStateChangedHandler
import com.ithersta.tgbotapi.fsm.repository.StateRepository
import com.ithersta.tgbotapi.fsm.tryHandlingHelp
import dev.inmo.micro_utils.coroutines.subscribeSafelyWithoutExceptionsAsync
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.utils.shortcuts.executeAsync
import dev.inmo.tgbotapi.requests.bot.SetMyCommands
import dev.inmo.tgbotapi.types.BotCommand
import dev.inmo.tgbotapi.types.commands.BotCommandScope
import dev.inmo.tgbotapi.types.update.abstracts.Update

typealias ExceptionHandler<K> = suspend RequestsExecutor.(K, Throwable) -> Unit

class StateMachine<BS : Any, BU : Any, K : Any>(
    private val filters: List<RoleFilter<BS, BU, *, K>>,
    private val includeHelp: Boolean,
    private val getKey: (Update) -> K?,
    private val getUser: (K) -> BU,
    private val getScope: (K) -> BotCommandScope,
    private val stateRepository: StateRepository<K, BS>,
    private val exceptionHandler: ExceptionHandler<K>?
) {
    fun BehaviourContext.collect() {
        allUpdatesFlow.subscribeSafelyWithoutExceptionsAsync(scope, { getKey(it) }) { update ->
            val key = getKey(update) ?: return@subscribeSafelyWithoutExceptionsAsync
            runCatching {
                val user = getUser(key)
                val state = stateRepository.get(key)
                if (includeHelp && tryHandlingHelp(update) { commands(user) }) {
                    return@runCatching
                }
                handler(update, user, state)?.invoke(bot, { setState(key, it) }, { refreshCommands(key) })
            }.onFailure {
                exceptionHandler?.invoke(bot, key, it)
            }
        }
    }

    suspend fun RequestsExecutor.setState(key: K, state: BS) {
        val user = getUser(key)
        stateRepository.set(key, state)
        onStateChangedHandlers(user, state).forEach { handler ->
            handler(this, key, { setState(key, it) }, { refreshCommands(key) })
        }
    }

    private suspend fun RequestsExecutor.refreshCommands(key: K) {
        @Suppress("DeferredResultUnused")
        executeAsync(SetMyCommands(commands(getUser(key)), getScope(key)))
    }

    private fun handler(update: Update, user: BU, state: BS): AppliedHandler<BS>? {
        return filters.firstNotNullOfOrNull { it.handler(user, update, state) }
    }

    private fun onStateChangedHandlers(
        user: BU,
        state: BS
    ): List<AppliedOnStateChangedHandler<BS, K>> {
        return filters.flatMap { it.onStateChangedHandlers(user, state) }
    }

    private fun commands(user: BU): List<BotCommand> {
        return filters.flatMap { it.commands(user) }
    }
}
