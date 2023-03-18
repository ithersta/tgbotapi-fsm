package com.ithersta.tgbotapi.menu.entities

import com.ithersta.tgbotapi.fsm.entities.triggers.Handler
import dev.inmo.tgbotapi.types.message.content.TextMessage

class MenuButton<BS : Any, BU : Any, U : BU> internal constructor(
    override val text: String,
    val description: String?,
    override val handler: Handler<BS, BU, *, U, TextMessage>
) : MenuEntry<BS, BU, U>
