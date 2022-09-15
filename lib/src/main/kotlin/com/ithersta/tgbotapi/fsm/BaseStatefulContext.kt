package com.ithersta.tgbotapi.fsm

import dev.inmo.tgbotapi.bot.RequestsExecutor
import kotlinx.coroutines.CoroutineScope

interface BaseStatefulContext<BS : Any, BU : Any, S : BS, U : BU> : RequestsExecutor {
    val state: S
    val setState: suspend (BS) -> Unit
    val setStateQuiet: (BS) -> Unit
    val refreshCommands: suspend () -> Unit
    val user: U
    val coroutineScope: CoroutineScope
}
