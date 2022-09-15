package com.ithersta.tgbotapi.pagination

import com.ithersta.tgbotapi.fsm.BaseStatefulContext
import com.ithersta.tgbotapi.fsm.builders.StateFilterBuilder
import com.ithersta.tgbotapi.fsm.entities.triggers.onDataCallbackQuery
import dev.inmo.tgbotapi.extensions.api.answers.answer
import dev.inmo.tgbotapi.extensions.api.edit.text.editMessageText
import dev.inmo.tgbotapi.extensions.api.send.sendTextMessage
import dev.inmo.tgbotapi.extensions.utils.asMessageCallbackQuery
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.MessageIdentifier
import dev.inmo.tgbotapi.types.UserId
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardMarkup
import dev.inmo.tgbotapi.utils.PreviewFeature
import kotlinx.serialization.Serializable

@Serializable
class PagerState(
    val page: Int = 0,
    val messageId: MessageIdentifier? = null
)

@OptIn(PreviewFeature::class)
class StatefulInlineKeyboardPager<BS : Any, BU : Any, S : BS, U : BU>(
    private val id: String,
    private val limit: Int = 5,
    private val onPagerStateChanged: BaseStatefulContext<BS, BU, S, U>.(PagerState) -> BS,
    private val block: context(PagerBuilder) BaseStatefulContext<BS, BU, S, U>.() -> InlineKeyboardMarkup
) {
    context(BaseStatefulContext<BS, BU, S, U>)
    suspend fun sendOrEditMessage(chatId: ChatId, text: String, pagerState: PagerState) {
        val page = pagerState.page
        val offset = page * limit
        val inlineKeyboard = block(PagerBuilder(page, offset, limit, id), this@BaseStatefulContext)
        if (pagerState.messageId == null) {
            val messageId = sendTextMessage(chatId, text, replyMarkup = inlineKeyboard).messageId
            setStateQuiet(onPagerStateChanged(this@BaseStatefulContext, PagerState(pagerState.page, messageId)))
        } else {
            runCatching {
                editMessageText(chatId, pagerState.messageId, text, replyMarkup = inlineKeyboard)
            }
        }
    }

    context(StateFilterBuilder<BS, BU, S, U, UserId>)
    fun setupTriggers() {
        onDataCallbackQuery(Regex("$PREFIX $id")) {
            setState(state)
            answer(it)
        }
        onDataCallbackQuery(Regex("$PREFIX $id page \\d+")) {
            val page = it.data.split(" ").last().toInt()
            val messageId = it.asMessageCallbackQuery()?.message?.messageId ?: return@onDataCallbackQuery
            val pagerState = PagerState(page, messageId)
            setState(onPagerStateChanged(pagerState))
            answer(it)
        }
    }
}

fun <BS : Any, BU : Any, S : BS, U : BU> StateFilterBuilder<BS, BU, S, U, UserId>.statefulInlineKeyboardPager(
    id: String,
    limit: Int = 5,
    onPagerStateChanged: BaseStatefulContext<BS, BU, S, U>.(PagerState) -> BS,
    block: context(PagerBuilder) BaseStatefulContext<BS, BU, S, U>.() -> InlineKeyboardMarkup
): StatefulInlineKeyboardPager<BS, BU, S, U> {
    return StatefulInlineKeyboardPager(id, limit, onPagerStateChanged, block).also {
        it.setupTriggers()
    }
}
