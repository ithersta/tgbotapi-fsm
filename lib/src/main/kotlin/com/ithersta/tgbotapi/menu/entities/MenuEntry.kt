package com.ithersta.tgbotapi.menu.entities

import com.ithersta.tgbotapi.fsm.entities.triggers.Handler
import dev.inmo.tgbotapi.types.message.content.TextMessage

sealed interface MenuEntry<BS : Any, BU : Any, U : BU> {
    val text: String
    val handler: Handler<BS, BU, *, U, TextMessage>
}
