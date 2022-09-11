package com.ithersta.tgbotapi.fsm

import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.types.update.abstracts.Update

class StatefulContext<BS : Any, BU : Any, S : BS, U : BU>(
    requestsExecutor: RequestsExecutor,
    override val state: S,
    override val setState: suspend (BS) -> Unit,
    override val setStateQuiet: (BS) -> Unit,
    override val refreshCommands: suspend () -> Unit,
    val update: Update,
    override val user: U
) : RequestsExecutor by requestsExecutor, BaseStatefulContext<BS, BU, S, U>
