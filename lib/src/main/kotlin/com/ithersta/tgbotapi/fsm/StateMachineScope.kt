package com.ithersta.tgbotapi.fsm

import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.types.update.abstracts.Update
import kotlin.reflect.KClass

internal typealias InternalHandler<BaseState> = suspend (Update, BaseState, (BaseState) -> Unit) -> Boolean

class StateMachineScope<BaseState : Any>(private val requestsExecutor: RequestsExecutor) {
    val handlers = mutableListOf<InternalHandler<BaseState>>()

    inline fun <reified S : BaseState> state(noinline block: StateScope<BaseState, S>.() -> Unit) {
        state(S::class, block)
    }

    fun <S : BaseState> state(type: KClass<S>, block: StateScope<BaseState, S>.() -> Unit) {
        block(StateScope(type, handlers, requestsExecutor))
    }
}
