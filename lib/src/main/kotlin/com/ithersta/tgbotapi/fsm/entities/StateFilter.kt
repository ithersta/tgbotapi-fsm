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
    private val nestedStateMachines: List<NestedStateMachine<BS, BU, U, K>>,
    private val onChangedTrigger: OnStateChangedTrigger<BS, BU, S, U, K>?,
    private val level: Int
) {
    private fun isApplicable(stateHolder: StateMachine<BS, *, *>.StateHolder<BS>) =
        map(stateHolder.stack[level]) != null

    fun handler(update: Update, stateHolder: StateMachine<BS, *, *>.StateHolder<BS>, user: U): AppliedHandler? {
        if (isApplicable(stateHolder).not()) return null
        return when (stateHolder.level) {
            level -> triggers.firstNotNullOfOrNull {
                it.handler(
                    update,
                    stateHolder as StateMachine<BS, *, *>.StateHolder<S>,
                    user
                )
            }

            else -> nestedStateMachines.firstNotNullOfOrNull { it.handler(update, stateHolder, user) }
        }
    }

    fun onStateChangedHandler(
        stateHolder: StateMachine<BS, *, *>.StateHolder<BS>,
        user: U
    ): List<AppliedOnStateChangedHandler<K>> {
        if (isApplicable(stateHolder).not()) return emptyList()
        return when (stateHolder.level) {
            level -> listOfNotNull(
                onChangedTrigger?.handler(
                    stateHolder as StateMachine<BS, *, *>.StateHolder<S>,
                    user
                )
            )

            else -> nestedStateMachines.flatMap { it.onStateChangedHandlers(user, stateHolder) }
        }
    }

    fun commands(): List<BotCommand> {
        return triggers.mapNotNull { it.botCommand } + nestedStateMachines.flatMap { it.commands() }
    }
}
