package com.ithersta.tgbotapi.menu.entities

open class Menu<BS : Any, BU : Any, U : BU>(
    val messageText: String,
    val state: BS,
    val entries: List<MenuEntry<BS, BU, U>>
)
