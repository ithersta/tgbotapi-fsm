package com.ithersta.tgbotapi.pagination

import com.ithersta.tgbotapi.fsm.BaseStatefulContext
import com.ithersta.tgbotapi.fsm.builders.RoleFilterBuilder
import com.ithersta.tgbotapi.fsm.entities.triggers.onDataCallbackQuery
import dev.inmo.tgbotapi.bot.exceptions.CommonBotException
import dev.inmo.tgbotapi.extensions.api.answers.answer
import dev.inmo.tgbotapi.extensions.api.edit.reply_markup.editMessageReplyMarkup
import dev.inmo.tgbotapi.extensions.utils.asMessageCallbackQuery
import dev.inmo.tgbotapi.types.UserId
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardMarkup
import dev.inmo.tgbotapi.utils.PreviewFeature

const val PREFIX = "com.ithersta.tgbotapi.pagination"

@OptIn(PreviewFeature::class)
class InlineKeyboardPager<BS : Any, BU : Any, U : BU>(
    private val id: String,
    private val limit: Int = 5,
    private val block: PagerBuilder<BS, BU, *, U>.() -> InlineKeyboardMarkup
) {
    val BaseStatefulContext<BS, BU, *, U>.firstPage
        get() = page(0)

    fun BaseStatefulContext<BS, BU, *, U>.page(index: Int) =
        block(PagerBuilder(index, index * limit, limit, id, this))

    fun RoleFilterBuilder<BS, BU, U, UserId>.setupTriggers() {
        anyState {
            onDataCallbackQuery(Regex("$PREFIX $id")) {
                answer(it)
            }
            onDataCallbackQuery(Regex("$PREFIX $id page \\d+")) {
                val pageIndex = it.data.split(" ").last().toInt()
                val message = it.asMessageCallbackQuery()?.message ?: return@onDataCallbackQuery
                try {
                    editMessageReplyMarkup(message, page(pageIndex))
                } catch (_: CommonBotException) {
                }
                answer(it)
            }
        }
    }
}

fun <BS : Any, BU : Any, U : BU> RoleFilterBuilder<BS, BU, U, UserId>.inlineKeyboardPager(
    id: String,
    limit: Int = 5,
    block: PagerBuilder<BS, BU, *, U>.() -> InlineKeyboardMarkup
): InlineKeyboardPager<BS, BU, U> {
    return InlineKeyboardPager(id, limit, block).also {
        with(it) { setupTriggers() }
    }
}
