package com.ithersta.tgbotapi.menu.builders

import com.ithersta.tgbotapi.fsm.entities.triggers.Handler
import com.ithersta.tgbotapi.menu.entities.Menu
import com.ithersta.tgbotapi.menu.entities.MenuButton
import com.ithersta.tgbotapi.menu.entities.MenuEntry
import dev.inmo.tgbotapi.types.message.content.TextMessage

open class MenuBuilder<BS : Any, BU : Any, U : BU>(protected val messageText: String, protected val state: BS) {
    protected val entries = mutableListOf<MenuEntry<BS, BU, U>>()

    fun submenu(text: String, messageText: String, state: BS, block: SubmenuBuilder<BS, BU, U>.() -> Unit) {
        entries.add(SubmenuBuilder<BS, BU, U>(text, messageText, state, this.state).apply(block).build())
    }

    fun button(text: String, state: BS, description: String? = null) {
        button(text, description) { setState(state) }
    }

    fun button(text: String, description: String? = null, handler: Handler<BS, BU, *, U, TextMessage>) {
        entries.add(MenuButton(text, description, handler))
    }

    open fun build(): Menu<BS, BU, U> {
        return Menu(messageText, state, entries)
    }
}

fun <BS : Any, BU : Any, U : BU> createMenu(
    messageText: String,
    state: BS,
    block: MenuBuilder<BS, BU, U>.() -> Unit
) = MenuBuilder<BS, BU, U>(messageText, state).apply(block).build()

