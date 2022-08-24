package com.ithersta.tgbotapi.fsm

import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.types.update.abstracts.Update

class StatefulContext<BS : Any, BU : Any, S : BS, U : BU>(
    requestsExecutor: RequestsExecutor,
    val state: S,
    val setState: suspend (BS) -> Unit,
    val update: Update,
    val user: U
) : RequestsExecutor by requestsExecutor
