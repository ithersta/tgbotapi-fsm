package com.ithersta.tgbotapi.fsm.entities.triggers

import com.ithersta.tgbotapi.fsm.builders.StateFilterBuilder
import dev.inmo.tgbotapi.bot.RequestsExecutor

class OnStateChangedContext<BS : Any, BU : Any, S : BS, U : BU>(
    requestsExecutor: RequestsExecutor,
    val state: S,
    val setState: suspend (BS) -> Unit,
    val user: U
) : RequestsExecutor by requestsExecutor

typealias OnStateChangedHandler<BS, BU, S, U, K> =
        suspend OnStateChangedContext<BS, BU, S, U>.(K) -> Unit

typealias AppliedOnStateChangedHandler<BS, K> =
        suspend (RequestsExecutor, K, suspend (BS) -> Unit) -> Unit

class OnStateChangedTrigger<BS : Any, BU : Any, S : BS, U : BU, K : Any>(
    private val handler: OnStateChangedHandler<BS, BU, S, U, K>
) {
    fun handler(state: S, user: U): AppliedOnStateChangedHandler<BS, K> {
        return { requestsExecutor, key, setState ->
            OnStateChangedContext<BS, BU, _, _>(requestsExecutor, state, setState, user).handler(key)
        }
    }
}

fun <BS : Any, BU : Any, S : BS, U : BU, K : Any> StateFilterBuilder<BS, BU, S, U, K>.onTransition(
    handler: OnStateChangedHandler<BS, BU, S, U, K>
) {
    set(OnStateChangedTrigger(handler))
}
