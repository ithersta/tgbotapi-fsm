package com.ithersta.tgbotapi.menu.builders

import com.ithersta.tgbotapi.menu.entities.Menu
import com.ithersta.tgbotapi.menu.entities.MenuButton
import com.ithersta.tgbotapi.menu.entities.MenuEntry

open class MenuBuilder<BS : Any>(protected val messageText: String, protected val state: BS) {
    protected val entries = mutableListOf<MenuEntry<BS>>()

    fun submenu(text: String, messageText: String, state: BS, block: SubmenuBuilder<BS>.() -> Unit) {
        entries.add(SubmenuBuilder(text, messageText, state, this.state).apply(block).build())
    }

    fun button(text: String, state: BS) {
        entries.add(MenuButton(text, state))
    }

    open fun build(): Menu<BS> {
        return Menu(messageText, state, entries)
    }
}

fun <BS : Any> createMenu(
    messageText: String,
    state: BS,
    block: MenuBuilder<BS>.() -> Unit
) = MenuBuilder(messageText, state).apply(block).build()

