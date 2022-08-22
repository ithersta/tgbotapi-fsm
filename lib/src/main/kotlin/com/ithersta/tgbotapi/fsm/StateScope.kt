package com.ithersta.tgbotapi.fsm

import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.types.update.abstracts.Update
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

internal typealias Handler<BaseState, S, U> = suspend StatefulContext<BaseState, S>.(U) -> Unit

class StateScope<BaseState : Any, S : BaseState>(
    private val type: KClass<S>,
    private val handlers: MutableList<InternalHandler<BaseState>>,
    private val requestsExecutor: RequestsExecutor
) {
    fun <Data> on(
        handler: Handler<BaseState, S, Data>,
        convertToData: (Update) -> List<Data>
    ) {
        handlers.add { anyUpdate, baseState, setState ->
            val state = type.safeCast(baseState) ?: return@add false
            convertToData(anyUpdate).forEach {
                handler(StatefulContext(requestsExecutor, state, setState), it)
            }
            true
        }
    }
}
