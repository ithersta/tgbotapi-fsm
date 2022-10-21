package com.ithersta.tgbotapi.fsm.entities.triggers

import com.ithersta.tgbotapi.fsm.BaseStatefulContext
import com.ithersta.tgbotapi.fsm.builders.StateFilterBuilder
import dev.inmo.tgbotapi.bot.RequestsExecutor
import kotlinx.coroutines.CoroutineScope

class OnStateChangedContext<BS : Any, BU : Any, S : BS, U : BU>(
    requestsExecutor: RequestsExecutor,
    override val state: S,
    override val setState: suspend (BS) -> Unit,
    override val setStateQuiet: (BS) -> Unit,
    override val refreshCommands: suspend () -> Unit,
    override val user: U,
    override val coroutineScope: CoroutineScope
) : RequestsExecutor by requestsExecutor, BaseStatefulContext<BS, BU, S, U>

typealias OnStateChangedHandler<BS, BU, S, U, K> =
        suspend OnStateChangedContext<BS, BU, S, U>.(K) -> Unit

typealias AppliedOnStateChangedHandler<BS, K> =
        suspend (RequestsExecutor, K, suspend (BS) -> Unit, (BS) -> Unit, suspend () -> Unit, CoroutineScope) -> Unit

class OnStateChangedTrigger<BS : Any, BU : Any, S : BS, U : BU, K : Any>(
    private val handler: OnStateChangedHandler<BS, BU, S, U, K>
) {
    fun handler(state: S, user: U): AppliedOnStateChangedHandler<BS, K> {
        return { requestsExecutor, key, setState, setStateQuiet, refreshCommands, coroutineScope ->
            OnStateChangedContext<BS, BU, _, _>(
                requestsExecutor,
                state,
                setState,
                setStateQuiet,
                refreshCommands,
                user,
                coroutineScope
            ).handler(key)
        }
    }
}

fun <BS : Any, BU : Any, S : BS, U : BU, K : Any> StateFilterBuilder<BS, BU, S, U, K>.onEnter(
    handler: OnStateChangedHandler<BS, BU, S, U, K>
) {
    set(OnStateChangedTrigger(handler))
}

@Deprecated(message = "onTransition is ambiguous. Use onEnter instead.", replaceWith = ReplaceWith("onEnter(handler)"))
fun <BS : Any, BU : Any, S : BS, U : BU, K : Any> StateFilterBuilder<BS, BU, S, U, K>.onTransition(
    handler: OnStateChangedHandler<BS, BU, S, U, K>
) = onEnter(handler)
