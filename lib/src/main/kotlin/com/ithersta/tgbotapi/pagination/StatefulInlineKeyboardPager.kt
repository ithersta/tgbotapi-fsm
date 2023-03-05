package com.ithersta.tgbotapi.pagination

import com.ithersta.tgbotapi.fsm.BaseStatefulContext
import com.ithersta.tgbotapi.fsm.builders.StateFilterBuilder
import com.ithersta.tgbotapi.fsm.entities.triggers.Trigger
import com.ithersta.tgbotapi.fsm.entities.triggers.onDataCallbackQuery
import dev.inmo.tgbotapi.extensions.api.answers.answer
import dev.inmo.tgbotapi.extensions.api.edit.text.editMessageText
import dev.inmo.tgbotapi.extensions.api.send.sendTextMessage
import dev.inmo.tgbotapi.extensions.utils.asCallbackQueryUpdate
import dev.inmo.tgbotapi.extensions.utils.asDataCallbackQuery
import dev.inmo.tgbotapi.extensions.utils.asMessageCallbackQuery
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.MessageIdentifier
import dev.inmo.tgbotapi.types.UserId
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardMarkup
import dev.inmo.tgbotapi.types.queries.callback.CallbackQuery
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
    private val block: PagerBuilder<Unit, BS, BU, S, U>.() -> InlineKeyboardMarkup
) {
    suspend fun BaseStatefulContext<BS, BU, S, U>.sendOrEditMessage(
        idChatIdentifier: IdChatIdentifier,
        text: String,
        pagerState: PagerState
    ) {
        val page = pagerState.page
        val offset = page * limit
        val inlineKeyboard = block(PagerBuilder(page, offset, limit, Unit, Unit::class, id, this))
        if (pagerState.messageId == null) {
            val messageId = sendTextMessage(idChatIdentifier, text, replyMarkup = inlineKeyboard).messageId
            state.overrideQuietly {
                onPagerStateChanged(
                    this@sendOrEditMessage,
                    PagerState(pagerState.page, messageId)
                )
            }
        } else {
            runCatching {
                editMessageText(idChatIdentifier, pagerState.messageId, text, replyMarkup = inlineKeyboard)
            }
        }
    }

    fun StateFilterBuilder<BS, BU, S, U, UserId>.setupTriggers() {
        add(
            Trigger<BS, BU, S, U, Pair<Int, CallbackQuery>>(
                handler = { (page, query) ->
                    val messageId = query.asMessageCallbackQuery()?.message?.messageId ?: return@Trigger
                    val pagerState = PagerState(page, messageId)
                    state.override { onPagerStateChanged(pagerState) }
                    answer(query)
                }
            ) {
                asCallbackQueryUpdate()?.data?.asDataCallbackQuery()
                    ?.takeIf { it.data.startsWith(PREFIX) }
                    ?.let {
                        runCatching {
                            val withoutPrefix = it.data.removePrefix(PREFIX)
                            check(withoutPrefix.startsWith(id))
                            val page = withoutPrefix.removePrefix(id).toInt()
                            page to it
                        }.getOrNull()
                    }
            }
        )
    }
}

fun <BS : Any, BU : Any, S : BS, U : BU> StateFilterBuilder<BS, BU, S, U, UserId>.statefulPager(
    id: String,
    limit: Int = 5,
    onPagerStateChanged: BaseStatefulContext<BS, BU, S, U>.(PagerState) -> BS,
    block: PagerBuilder<Unit, BS, BU, S, U>.() -> InlineKeyboardMarkup
): StatefulInlineKeyboardPager<BS, BU, S, U> {
    return StatefulInlineKeyboardPager(id, limit, onPagerStateChanged, block).also {
        with(it) { setupTriggers() }
    }
}
