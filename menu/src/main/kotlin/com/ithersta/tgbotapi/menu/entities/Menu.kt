package com.ithersta.tgbotapi.menu.entities

open class Menu<BS : Any, BU : Any, U : BU>(
    val messageText: String,
    val state: BS,
    val entries: List<MenuEntry<BS, BU, U>>
) {
    val flattenedEntries: List<MenuEntry<BS, BU, U>> = entries.flatMap {
        when (it) {
            is MenuButton -> listOf(it)
            is Submenu -> it.flattenedEntries
        }
    }
    val submenus = flattenedEntries.filterIsInstance<Submenu<BS, BU, U>>()
    val buttons by lazy { flattenedEntries.filterIsInstance<MenuButton<BS, BU, U>>() }
    val descriptions by lazy { buttons.mapNotNull { it.description } }
}
