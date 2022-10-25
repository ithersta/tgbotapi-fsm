package com.ithersta.tgbotapi.fsm

import com.ithersta.tgbotapi.fsm.entities.StateMachine
import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.types.update.abstracts.Update
import kotlinx.coroutines.CoroutineScope

class StatefulContext<BS : Any, BU : Any, S : BS, U : BU>(
    requestsExecutor: RequestsExecutor,
    override val state: StateMachine<BS, *, *>.StateHolder<S>,
    override val refreshCommands: suspend () -> Unit,
    val update: Update,
    override val user: U,
    override val coroutineScope: CoroutineScope
) : RequestsExecutor by requestsExecutor, BaseStatefulContext<BS, BU, S, U>
