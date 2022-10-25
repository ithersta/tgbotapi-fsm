package com.ithersta.tgbotapi.menu.builders

import com.ithersta.tgbotapi.menu.entities.Submenu

class SubmenuBuilder<BS : Any, BU : Any, U : BU>(
    private val text: String,
    messageText: String,
    state: BS,
    private val parentState: BS
) : MenuBuilder<BS, BU, U>(messageText, state) {
    fun backButton(text: String) {
        button(text, parentState)
    }

    override fun build(): Submenu<BS, BU, U> {
        return Submenu(text, messageText, targetState, entries)
    }
}
