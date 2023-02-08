package com.ithersta.tgbotapi.fsm.entities

import com.ithersta.tgbotapi.fsm.entities.triggers.AppliedHandler
import com.ithersta.tgbotapi.fsm.entities.triggers.AppliedOnStateChangedHandler
import com.ithersta.tgbotapi.fsm.engines.repository.StateRepository
import com.ithersta.tgbotapi.fsm.tryHandlingHelp
import dev.inmo.micro_utils.coroutines.subscribeSafelyWithoutExceptionsAsync
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.utils.shortcuts.executeAsync
import dev.inmo.tgbotapi.requests.bot.SetMyCommands
import dev.inmo.tgbotapi.types.BotCommand
import dev.inmo.tgbotapi.types.commands.BotCommandScope
import dev.inmo.tgbotapi.types.update.abstracts.Update

class StateMachine<BS : Any, BU : Any, K : Any>(
    private val filters: List<RoleFilter<BS, BU, *, K>>,
    private val includeHelp: Boolean,
    private val initialState: BS,
) {
    inner class DataAccessor(
        val key: K,
        val getUser: () -> BU,
        private val getStateStackData: () -> List<BS>?,
        private val setStateStackData: (List<BS>) -> Unit,
        private val rollbackStateStackData: () -> List<BS>?,
        val refreshCommands: suspend (List<BotCommand>) -> Unit
    ) {
        fun getStateStack() = getStateStackData() ?: listOf(initialState)
        fun setStateStackQuietly(stateStack: List<BS>) = setStateStackData(stateStack)

        suspend fun BehaviourContext.setStateStack(
            stateStack: List<BS>
        ) {
            setStateStackData(stateStack)
            onStateStackUpdated(stateStack, false)
        }

        private suspend fun BehaviourContext.onStateStackUpdated(
            stateStack: List<BS>,
            isRollingBack: Boolean
        ) {
            val user = getUser()
            val stateHolder = StateHolder<BS>(stateStack, this, this@DataAccessor, isRollingBack)
            onStateChangedHandlers(user, stateHolder).forEach { handler ->
                handler.invoke(bot, key, { refreshCommands(commands(user)) }, this)
            }
        }

        suspend fun BehaviourContext.rollbackStateStack(): Boolean {
            val stateStack = rollbackStateStackData() ?: return false
            onStateStackUpdated(stateStack, true)
            return true
        }
    }

    inner class StateHolder<S : BS>(
        val stack: List<BS>,
        private val behaviourContext: BehaviourContext,
        private val dataAccessor: DataAccessor,
        val isRollingBack: Boolean
    ) {
        val snapshot: S get() = stack.last() as S
        val level: Int get() = stack.lastIndex

        suspend fun push(state: BS) {
            val stateStack = dataAccessor.getStateStack()
            check(stateStack.lastIndex == level)
            with(dataAccessor) {
                behaviourContext.setStateStack(stateStack + state)
            }
        }

        suspend fun popAndOverride(override: (BS) -> BS) {
            val stateStack = dataAccessor.getStateStack()
            check(stateStack.lastIndex == level)
            val newStateStack = stateStack.dropLast(1).let {
                it.dropLast(1) + override(it.last())
            }
            with(dataAccessor) {
                behaviourContext.setStateStack(newStateStack)
            }
        }

        suspend fun override(block: S.() -> BS) {
            with(dataAccessor) {
                behaviourContext.setStateStack(overridden(block))
            }
        }

        fun overrideQuietly(block: S.() -> BS) {
            dataAccessor.setStateStackQuietly(overridden(block))
        }

        suspend fun rollback(): Boolean {
            return with(dataAccessor) {
                behaviourContext.rollbackStateStack()
            }
        }

        private fun overridden(block: S.() -> BS): List<BS> {
            val stateStack = dataAccessor.getStateStack()
            check(stateStack.lastIndex == level)
            return stateStack.dropLast(1) + block(snapshot)
        }
    }

    suspend fun BehaviourContext.dispatch(
        update: Update,
        dataAccessor: DataAccessor
    ) {
        val user = dataAccessor.getUser()
        if (includeHelp && tryHandlingHelp(update) { commands(user) }) return
        val stateHolder = StateHolder<BS>(
            stack = dataAccessor.getStateStack(),
            behaviourContext = this,
            dataAccessor = dataAccessor,
            isRollingBack = false
        )
        handler(update, user, stateHolder)?.invoke(bot, { dataAccessor.refreshCommands(commands(user)) }, this)
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
}
