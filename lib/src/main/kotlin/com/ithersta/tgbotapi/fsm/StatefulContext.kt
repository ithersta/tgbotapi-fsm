package com.ithersta.tgbotapi.fsm

import dev.inmo.tgbotapi.bot.RequestsExecutor

class StatefulContext<BaseState: Any, S : BaseState>(
    requestsExecutor: RequestsExecutor,
    val state: S,
    val setState: suspend (BaseState) -> Unit
) : RequestsExecutor by requestsExecutor
