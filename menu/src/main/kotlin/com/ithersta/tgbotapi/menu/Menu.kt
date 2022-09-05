package com.ithersta.tgbotapi.menu

import com.ithersta.tgbotapi.fsm.builders.RoleFilterBuilder
import com.ithersta.tgbotapi.fsm.entities.triggers.onText
import com.ithersta.tgbotapi.fsm.entities.triggers.onTransition
import com.ithersta.tgbotapi.menu.builders.MenuBuilder
import com.ithersta.tgbotapi.menu.builders.createMenu
import dev.inmo.tgbotapi.extensions.api.send.sendTextMessage
import dev.inmo.tgbotapi.extensions.utils.types.buttons.replyKeyboard
import dev.inmo.tgbotapi.extensions.utils.types.buttons.row
import dev.inmo.tgbotapi.extensions.utils.types.buttons.simpleButton
import dev.inmo.tgbotapi.types.UserId
import kotlin.reflect.safeCast

fun <BS : Any, BU : Any, U : BU> RoleFilterBuilder<BS, BU, U, UserId>.menu(
    textMessage: String,
    menuState: BS,
    block: MenuBuilder<BS, BU, U>.() -> Unit
) = createMenu(textMessage, menuState, block).also { menu ->
    menu.submenus.forEach { submenu ->
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
                    onText(entry.text, handler = entry.handler)
                }
            },
            map = { submenu.state::class.safeCast(it) }
        )
    }
}
