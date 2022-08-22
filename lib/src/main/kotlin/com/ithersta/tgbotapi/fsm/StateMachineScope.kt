package com.ithersta.tgbotapi.fsm

import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.types.update.abstracts.Update
import kotlin.reflect.KClass

internal typealias InternalHandler<BaseState> = suspend (Update, BaseState, suspend (BaseState) -> Unit) -> Boolean

class CommandEntry<BaseState : Any>(
    val command: String,
    val description: String,
    val type: KClass<BaseState>
)

class StateMachineScope<BaseState : Any>(private val requestsExecutor: RequestsExecutor) {
    private val _handlers = mutableListOf<InternalHandler<BaseState>>()
    private val _commands = mutableListOf<CommandEntry<out BaseState>>()
    val handlers: List<InternalHandler<BaseState>> = _handlers
    val commands: List<CommandEntry<out BaseState>> = _commands

    inline fun <reified S : BaseState> state(noinline block: StateScope<BaseState, S>.() -> Unit) {
        state(S::class, block)
    }

    fun <S : BaseState> state(type: KClass<S>, block: StateScope<BaseState, S>.() -> Unit) {
        block(StateScope(type, { _handlers.add(it) }, { _commands.add(it) }, requestsExecutor, this))
    }
}
