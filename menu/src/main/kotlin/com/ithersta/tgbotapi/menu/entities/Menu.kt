package com.ithersta.tgbotapi.menu.entities

open class Menu<BS : Any>(
    val messageText: String,
    val state: BS,
    val entries: List<MenuEntry<BS>>
)
