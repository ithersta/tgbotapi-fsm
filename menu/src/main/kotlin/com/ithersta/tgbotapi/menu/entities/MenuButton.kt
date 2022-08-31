package com.ithersta.tgbotapi.menu.entities

class MenuButton<BS : Any>(
    override val text: String,
    override val state: BS
) : MenuEntry<BS>
