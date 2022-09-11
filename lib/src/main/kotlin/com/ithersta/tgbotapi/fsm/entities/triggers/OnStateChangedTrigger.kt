package com.ithersta.tgbotapi.fsm.entities.triggers

import com.ithersta.tgbotapi.fsm.BaseStatefulContext
import com.ithersta.tgbotapi.fsm.builders.StateFilterBuilder
import dev.inmo.tgbotapi.bot.RequestsExecutor

class OnStateChangedContext<BS : Any, BU : Any, S : BS, U : BU>(
    requestsExecutor: RequestsExecutor,
    override val state: S,
    override val setState: suspend (BS) -> Unit,
    override val refreshCommands: suspend () -> Unit,
    override val user: U
) : RequestsExecutor by requestsExecutor, BaseStatefulContext<BS, BU, S, U>

typealias OnStateChangedHandler<BS, BU, S, U, K> =
        suspend OnStateChangedContext<BS, BU, S, U>.(K) -> Unit

typealias AppliedOnStateChangedHandler<BS, K> =
        suspend (RequestsExecutor, K, suspend (BS) -> Unit, suspend () -> Unit) -> Unit

class OnStateChangedTrigger<BS : Any, BU : Any, S : BS, U : BU, K : Any>(
    private val handler: OnStateChangedHandler<BS, BU, S, U, K>
) {
    fun handler(state: S, user: U): AppliedOnStateChangedHandler<BS, K> {
        return { requestsExecutor, key, setState, refreshCommands ->
            OnStateChangedContext<BS, BU, _, _>(requestsExecutor, state, setState, refreshCommands, user).handler(key)
        }
    }
}

fun <BS : Any, BU : Any, S : BS, U : BU, K : Any> StateFilterBuilder<BS, BU, S, U, K>.onTransition(
    handler: OnStateChangedHandler<BS, BU, S, U, K>
) {
    set(OnStateChangedTrigger(handler))
}
