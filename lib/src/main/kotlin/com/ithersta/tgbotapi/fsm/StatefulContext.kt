package com.ithersta.tgbotapi.fsm

import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.types.update.abstracts.Update

class StatefulContext<BaseState: Any, S : BaseState>(
    requestsExecutor: RequestsExecutor,
    val state: S,
    val setState: suspend (BaseState) -> Unit,
    val update: Update
) : RequestsExecutor by requestsExecutor
