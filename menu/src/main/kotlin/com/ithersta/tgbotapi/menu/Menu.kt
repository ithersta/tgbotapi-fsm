package com.ithersta.tgbotapi.menu

import com.ithersta.tgbotapi.fsm.builders.RoleFilterBuilder
import com.ithersta.tgbotapi.fsm.entities.triggers.onText
import com.ithersta.tgbotapi.fsm.entities.triggers.onTransition
import com.ithersta.tgbotapi.menu.builders.MenuBuilder
import com.ithersta.tgbotapi.menu.builders.createMenu
import com.ithersta.tgbotapi.menu.entities.Menu
import com.ithersta.tgbotapi.menu.entities.MenuEntry
import dev.inmo.tgbotapi.extensions.api.send.sendTextMessage
import dev.inmo.tgbotapi.extensions.utils.types.buttons.replyKeyboard
import dev.inmo.tgbotapi.extensions.utils.types.buttons.row
import dev.inmo.tgbotapi.extensions.utils.types.buttons.simpleButton
import dev.inmo.tgbotapi.types.UserId
import java.util.*
import kotlin.reflect.safeCast

fun <BS : Any> RoleFilterBuilder<BS, *, *, UserId>.menu(
    textMessage: String,
    menuState: BS,
    block: MenuBuilder<BS>.() -> Unit
) {
    val menu = createMenu(textMessage, menuState, block)
    val submenus = mutableSetOf(menu)
    val queue: Queue<MenuEntry<BS>> = LinkedList()
    queue.addAll(menu.entries)
    while (queue.isNotEmpty()) {
        val entry = queue.poll()
        if (entry is Menu<*>) {
            @Suppress("UNCHECKED_CAST")
            submenus.add(entry as Menu<BS>)
            queue.addAll(entry.entries)
        }
    }
    submenus.forEach { submenu ->
        state(
            block = {
                onTransition {
                    sendTextMessage(it, submenu.messageText, replyMarkup = replyKeyboard(resizeKeyboard = true) {
                        submenu.entries.forEach { entry ->
                            row {
                                simpleButton(entry.text)
                            }
                        }
                    })
                }
                submenu.entries.forEach { entry ->
                    onText(entry.text) {
                        setState(entry.state)
                    }
                }
            },
            map = { submenu.state::class.safeCast(it) }
        )
    }
}
