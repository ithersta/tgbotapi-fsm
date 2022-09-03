package com.ithersta.tgbotapi.menu.entities

import com.ithersta.tgbotapi.fsm.entities.triggers.Handler
import dev.inmo.tgbotapi.types.message.content.TextMessage

class Submenu<BS : Any, BU : Any, U : BU>(
    override val text: String,
    messageText: String,
    state: BS,
    entries: List<MenuEntry<BS, BU, U>>
) : Menu<BS, BU, U>(messageText, state, entries), MenuEntry<BS, BU, U> {
    override val handler: Handler<BS, BU, *, U, TextMessage>
        get() = { setState(this@Submenu.state) }
}
