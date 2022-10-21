package com.ithersta.tgbotapi.menu.entities

import com.ithersta.tgbotapi.fsm.builders.RoleFilterBuilder
import com.ithersta.tgbotapi.fsm.entities.triggers.onEnter
import com.ithersta.tgbotapi.fsm.entities.triggers.onText
import dev.inmo.tgbotapi.extensions.api.send.sendTextMessage
import dev.inmo.tgbotapi.extensions.utils.types.buttons.replyKeyboard
import dev.inmo.tgbotapi.extensions.utils.types.buttons.simpleButton
import dev.inmo.tgbotapi.types.UserId
import dev.inmo.tgbotapi.utils.row
import kotlin.reflect.safeCast

open class Menu<BS : Any, BU : Any, U : BU>(
    val messageText: String,
    val state: BS,
    val entries: List<MenuEntry<BS, BU, U>>
) {
    val flattenedEntries: List<MenuEntry<BS, BU, U>> = entries.flatMap {
        listOf(it) + when (it) {
            is MenuButton -> emptyList()
            is Submenu -> it.flattenedEntries
        }
    }
    val submenus by lazy { listOf(this) + flattenedEntries.filterIsInstance<Submenu<BS, BU, U>>() }
    val buttons by lazy { flattenedEntries.filterIsInstance<MenuButton<BS, BU, U>>() }
    val descriptions by lazy { buttons.mapNotNull { it.description } }

    operator fun RoleFilterBuilder<BS, BU, U, UserId>.invoke() {
        submenus.forEach { submenu ->
            state(
                block = {
                    onEnter {
                        sendTextMessage(it, submenu.messageText, replyMarkup = replyKeyboard(resizeKeyboard = true) {
                            submenu.entries.forEach { entry ->
                                row {
                                    simpleButton(entry.text)
                                }
                            }
                        })
                    }
                    submenu.entries.forEach { entry ->
                        onText(entry.text, handler = entry.handler)
                    }
                },
                map = { submenu.state::class.safeCast(it) }
            )
        }
    }
}
