package com.ithersta.tgbotapi.fsm

import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.types.update.abstracts.Update
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

internal typealias Handler<BaseState, S, U> = suspend StatefulContext<BaseState, S>.(U) -> Unit

class StateScope<BaseState : Any, S : BaseState> internal constructor(
    private val type: KClass<S>,
    private val addHandler: (InternalHandler<BaseState>) -> Unit,
    private val addCommand: (CommandEntry<out BaseState>) -> Unit,
    private val requestsExecutor: RequestsExecutor,
    val parentScope: StateMachineScope<BaseState>
) {
    fun <Data> on(
        handler: Handler<BaseState, S, Data>,
        convertToData: (Update) -> List<Data>
    ) {
        addHandler { anyUpdate, baseState, setState ->
            val state = type.safeCast(baseState) ?: return@addHandler false
            convertToData(anyUpdate).ifEmpty { return@addHandler false }.forEach {
                handler(StatefulContext(requestsExecutor, state, setState), it)
            }
            true
        }
    }

    internal fun addCommand(command: String, description: String) {
        addCommand(CommandEntry(command, description, type))
    }
}
