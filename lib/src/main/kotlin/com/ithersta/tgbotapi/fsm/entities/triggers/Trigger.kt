package com.ithersta.tgbotapi.fsm.entities.triggers

import com.ithersta.tgbotapi.fsm.StatefulContext
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.types.BotCommand
import dev.inmo.tgbotapi.types.update.abstracts.Update

typealias AppliedHandler<BaseState> = suspend (RequestsExecutor, suspend (BaseState) -> Unit) -> Unit
typealias Handler<BaseState, S, Data> = suspend StatefulContext<BaseState, S>.(Data) -> Unit

class Trigger<BaseState : Any, S : BaseState, Data>(
    private val handler: Handler<BaseState, S, Data>,
    val botCommand: BotCommand? = null,
    private val toData: Update.() -> Data?
) {
    fun handler(update: Update, state: S): AppliedHandler<BaseState>? {
        val data = update.toData() ?: return null
        return { requestsExecutor, setState ->
            StatefulContext<BaseState, S>(requestsExecutor, state, setState, update).handler(data)
        }
    }
}
