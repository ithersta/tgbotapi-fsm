package com.ithersta.tgbotapi.menu.entities

sealed interface MenuEntry<BS : Any> {
    val text: String
    val state: BS
}
