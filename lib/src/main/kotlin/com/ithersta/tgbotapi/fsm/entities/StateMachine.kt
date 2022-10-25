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
    private val initialState: BS,
    private val exceptionHandler: ExceptionHandler<K>?
) {
    fun BehaviourContext.collect() {
        allUpdatesFlow.subscribeSafelyWithoutExceptionsAsync(scope, { getKey(it) }) { update ->
            val key = getKey(update) ?: return@subscribeSafelyWithoutExceptionsAsync
            runCatching {
                val user = getUser(key)
                if (includeHelp && tryHandlingHelp(update) { commands(user) }) {
                    return@runCatching
                }
                val stateStack = getStateStack(key)
                val stateHolder = StateHolder<BS>(stateStack, key, this@collect)
                handler(update, user, stateHolder)?.invoke(
                    bot,
                    { refreshCommands(key) },
                    this
                )
            }.onFailure {
                exceptionHandler?.invoke(bot, key, it)
            }
        }
    }

    private suspend fun RequestsExecutor.refreshCommands(key: K) {
        @Suppress("DeferredResultUnused")
        executeAsync(SetMyCommands(commands(getUser(key)), getScope(key)))
    }

    private fun handler(update: Update, user: BU, stateHolder: StateHolder<BS>): AppliedHandler? {
        return filters.firstNotNullOfOrNull { it.handler(user, update, stateHolder) }
    }

    private fun onStateChangedHandlers(user: BU, stateHolder: StateHolder<BS>): List<AppliedOnStateChangedHandler<K>> {
        return filters.flatMap { it.onStateChangedHandlers(user, stateHolder) }
    }

    private fun commands(user: BU): List<BotCommand> {
        return filters.flatMap { it.commands(user) }
    }

    private fun getStateStack(key: K): List<BS> {
        return stateRepository.get(key) ?: listOf(initialState)
    }

    private suspend fun BehaviourContext.setStateStack(key: K, stateStack: List<BS>) {
        setStateStackQuietly(key, stateStack)
        val user = getUser(key)
        val stateHolder = StateHolder<BS>(stateStack, key, this)
        onStateChangedHandlers(user, stateHolder).forEach { handler ->
            handler.invoke(bot, key, { refreshCommands(key) }, this)
        }
    }

    private fun setStateStackQuietly(key: K, stateStack: List<BS>) {
        stateRepository.set(key, stateStack)
    }

    inner class StateHolder<S : BS>(
        val stack: List<BS>,
        private val key: K,
        private val behaviourContext: BehaviourContext
    ) {
        val snapshot: S get() = stack.last() as S
        val level: Int get() = stack.lastIndex

        suspend fun push(state: BS) {
            val stateStack = getStateStack(key)
            check(stateStack.lastIndex == level)
            with(behaviourContext) {
                setStateStack(key, stateStack + state)
            }
        }

        suspend fun popAndOverride(override: (BS) -> BS) {
            val stateStack = getStateStack(key)
            check(stateStack.lastIndex == level)
            val newStateStack = stateStack.dropLast(1).let {
                it.dropLast(1) + override(it.last())
            }
            with(behaviourContext) {
                setStateStack(key, newStateStack)
            }
        }

        suspend fun override(block: S.() -> BS) {
            with(behaviourContext) {
                setStateStack(key, overridden(block))
            }
        }

        fun overrideQuietly(block: S.() -> BS) {
            setStateStackQuietly(key, overridden(block))
        }

        private fun overridden(block: S.() -> BS): List<BS> {
            val stateStack = getStateStack(key)
            check(stateStack.lastIndex == level)
            return stateStack.dropLast(1) + block(snapshot)
        }
    }
}
