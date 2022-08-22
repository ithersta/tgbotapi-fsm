package com.ithersta.tgbotapi.fsm

import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.utils.commonMessageOrNull
import dev.inmo.tgbotapi.extensions.utils.messageUpdateOrNull
import dev.inmo.tgbotapi.extensions.utils.withContent
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.message.content.TextMessage
import dev.inmo.tgbotapi.types.update.abstracts.Update
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

internal typealias Handler<BaseState, S, U> = suspend StatefulContext<BaseState, S>.(U) -> Unit

class StateScope<BaseState : Any, S : BaseState>(
    private val type: KClass<S>,
    private val handlers: MutableList<InternalHandler<BaseState>>,
    private val requestsExecutor: RequestsExecutor
) {
    internal fun <U> on(
        handler: Handler<BaseState, S, U>,
        filterUpdate: (Update) -> U?
    ) {
        handlers.add { anyUpdate, baseState, setState ->
            val state = type.safeCast(baseState) ?: return@add false
            val update = filterUpdate(anyUpdate) ?: return@add false
            handler(StatefulContext(requestsExecutor, state, setState), update)
            true
        }
    }
}
