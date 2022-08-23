package com.ithersta.tgbotapi.fsm.entities.triggers

import com.ithersta.tgbotapi.fsm.builders.StateFilterBuilder
import dev.inmo.tgbotapi.bot.RequestsExecutor

class OnStateChangedContext<BaseState : Any, S : BaseState>(
    requestsExecutor: RequestsExecutor,
    val state: S,
    val setState: suspend (BaseState) -> Unit
) : RequestsExecutor by requestsExecutor

typealias OnStateChangedHandler<BaseState, S, Key> =
        suspend OnStateChangedContext<BaseState, S>.(Key) -> Unit

typealias AppliedOnStateChangedHandler<BaseState, Key> =
        suspend (RequestsExecutor, Key, suspend (BaseState) -> Unit) -> Unit

class OnStateChangedTrigger<BaseState : Any, S : BaseState, Key : Any>(
    private val handler: OnStateChangedHandler<BaseState, S, Key>
) {
    fun handler(state: S): AppliedOnStateChangedHandler<BaseState, Key> {
        return { requestsExecutor, key, setState ->
            OnStateChangedContext<BaseState, S>(requestsExecutor, state, setState).handler(key)
        }
    }
}

fun <BaseState : Any, S : BaseState, Key : Any> StateFilterBuilder<BaseState, S, Key>.onTransition(
    handler: OnStateChangedHandler<BaseState, S, Key>
) {
    set(OnStateChangedTrigger(handler))
}
