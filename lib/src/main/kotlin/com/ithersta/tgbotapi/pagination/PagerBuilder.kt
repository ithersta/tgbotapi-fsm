package com.ithersta.tgbotapi.pagination

import dev.inmo.tgbotapi.extensions.utils.types.buttons.InlineKeyboardBuilder
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.utils.row

class PagerBuilder<BS : Any, BU : Any, S : BS, U : BU>(
    val page: Int,
    val offset: Int,
    val limit: Int,
    private val id: String
) {
    fun InlineKeyboardBuilder.navigationRow(itemCount: Int, previous: String = "⬅️", next: String = "➡️") {
        val maxPage = ((itemCount - 1) / limit).coerceAtLeast(0)
        if (maxPage == 0 && page == 0) return
        row {
            if (page > 0) {
                dataButton(previous, "$PREFIX $id page ${(page - 1).coerceAtLeast(0)}")
            } else {
                dataButton(" ", "$PREFIX $id")
            }
            dataButton("${page + 1}/${maxPage + 1}", "$PREFIX $id page ${page.coerceIn(0, maxPage)}")
            if (page < maxPage) {
                dataButton(next, "$PREFIX $id page ${(page + 1).coerceAtMost(maxPage)}")
            } else {
                dataButton(" ", "$PREFIX $id")
            }
        }
    }
}
