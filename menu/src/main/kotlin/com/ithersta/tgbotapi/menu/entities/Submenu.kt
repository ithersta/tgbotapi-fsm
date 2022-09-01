package com.ithersta.tgbotapi.menu.entities

class Submenu<BS : Any>(
    override val text: String,
    messageText: String,
    state: BS,
    entries: List<MenuEntry<BS>>
) : Menu<BS>(messageText, state, entries), MenuEntry<BS>
