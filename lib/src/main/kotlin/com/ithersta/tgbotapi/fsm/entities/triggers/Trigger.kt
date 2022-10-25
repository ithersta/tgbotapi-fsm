package com.ithersta.tgbotapi.fsm.entities.triggers

import com.ithersta.tgbotapi.fsm.StatefulContext
import com.ithersta.tgbotapi.fsm.entities.StateMachine
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.types.BotCommand
import dev.inmo.tgbotapi.types.update.abstracts.Update
import kotlinx.coroutines.CoroutineScope

typealias AppliedHandler = suspend (RequestsExecutor, suspend () -> Unit, CoroutineScope) -> Unit
typealias Handler<BS, BU, S, U, D> = suspend StatefulContext<BS, BU, S, U>.(D) -> Unit

class Trigger<BS : Any, BU : Any, S : BS, U : BU, D>(
    private val handler: Handler<BS, BU, S, U, D>,
    val botCommand: BotCommand? = null,
    private val toData: Update.() -> D?
) {
    fun handler(update: Update, stateHolder: StateMachine<BS, *, *>.StateHolder<S>, user: U): AppliedHandler? {
        val data = update.toData() ?: return null
        return { requestsExecutor, refreshCommands, coroutineScope ->
            StatefulContext<BS, BU, S, U>(
                requestsExecutor,
                stateHolder,
                refreshCommands,
                update,
                user,
                coroutineScope
            ).handler(data)
        }
    }
}
