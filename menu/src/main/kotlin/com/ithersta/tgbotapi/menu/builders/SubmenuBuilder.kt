package com.ithersta.tgbotapi.menu.builders

import com.ithersta.tgbotapi.menu.entities.Submenu

class SubmenuBuilder<BS : Any>(
    private val text: String,
    messageText: String,
    state: BS,
    private val parentState: BS
) : MenuBuilder<BS>(messageText, state) {
    fun backButton(text: String) {
        button(text, parentState)
    }

    override fun build(): Submenu<BS> {
        return Submenu(text, messageText, state, entries)
    }
}
