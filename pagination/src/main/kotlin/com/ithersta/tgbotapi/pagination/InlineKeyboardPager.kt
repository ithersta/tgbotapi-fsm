package com.ithersta.tgbotapi.pagination

import com.ithersta.tgbotapi.fsm.BaseStatefulContext
import com.ithersta.tgbotapi.fsm.builders.RoleFilterBuilder
import com.ithersta.tgbotapi.fsm.entities.triggers.onDataCallbackQuery
import dev.inmo.tgbotapi.bot.exceptions.CommonBotException
import dev.inmo.tgbotapi.extensions.api.answers.answer
import dev.inmo.tgbotapi.extensions.api.edit.reply_markup.editMessageReplyMarkup
import dev.inmo.tgbotapi.extensions.utils.asMessageCallbackQuery
import dev.inmo.tgbotapi.extensions.utils.types.buttons.InlineKeyboardBuilder
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.extensions.utils.types.buttons.row
import dev.inmo.tgbotapi.types.UserId
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardMarkup
import dev.inmo.tgbotapi.utils.PreviewFeature

private const val PREFIX = "com.ithersta.tgbotapi.pagination"

@OptIn(PreviewFeature::class)
class InlineKeyboardPager<BS : Any, BU : Any, U : BU>(
    private val id: String,
    private val limit: Int = 5,
    private val block: context(BaseStatefulContext<BS, BU, *, U>, Builder)(offset: Int, limit: Int) -> InlineKeyboardMarkup
) {
    context(BaseStatefulContext<BS, BU, *, U>)
    val firstPage
        get() = block(this@BaseStatefulContext, Builder(0, limit, id), 0, limit)

    context(RoleFilterBuilder<BS, BU, U, UserId>)
    fun setupTriggers() {
        anyState {
            onDataCallbackQuery(Regex("$PREFIX $id")) {
                answer(it)
            }
            onDataCallbackQuery(Regex("$PREFIX $id page \\d+")) {
                val page = it.data.split(" ").last().toInt()
                val offset = page * limit
                val inlineKeyboardMarkup = block(this, Builder(page, limit, id), offset, limit)
                val message = it.asMessageCallbackQuery()?.message ?: return@onDataCallbackQuery
                try {
                    editMessageReplyMarkup(message, inlineKeyboardMarkup)
                } catch (_: CommonBotException) {
                }
                answer(it)
            }
        }
    }

    class Builder(private val page: Int, private val limit: Int, private val id: String) {
        context(InlineKeyboardBuilder)
        fun navigationRow(itemCount: Int, previous: String = "⬅️", next: String = "➡️") {
            val maxPage = itemCount / limit
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
}

fun <BS : Any, BU : Any, U : BU> RoleFilterBuilder<BS, BU, U, UserId>.inlineKeyboardPager(
    id: String,
    limit: Int = 5,
    block: context(BaseStatefulContext<BS, BU, *, U>, InlineKeyboardPager.Builder) (offset: Int, limit: Int) -> InlineKeyboardMarkup
): InlineKeyboardPager<BS, BU, U> {
    return InlineKeyboardPager(id, limit, block).also {
        it.setupTriggers()
    }
}
