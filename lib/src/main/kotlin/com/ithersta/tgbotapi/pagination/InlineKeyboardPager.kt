package com.ithersta.tgbotapi.pagination

import com.ithersta.tgbotapi.encoder.Base122
import com.ithersta.tgbotapi.fsm.BaseStatefulContext
import com.ithersta.tgbotapi.fsm.builders.RoleFilterBuilder
import com.ithersta.tgbotapi.fsm.entities.triggers.Trigger
import dev.inmo.tgbotapi.bot.exceptions.MessageIsNotModifiedException
import dev.inmo.tgbotapi.extensions.api.answers.answer
import dev.inmo.tgbotapi.extensions.api.edit.reply_markup.editMessageReplyMarkup
import dev.inmo.tgbotapi.extensions.utils.asCallbackQueryUpdate
import dev.inmo.tgbotapi.extensions.utils.asDataCallbackQuery
import dev.inmo.tgbotapi.extensions.utils.asMessageCallbackQuery
import dev.inmo.tgbotapi.types.UserId
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardMarkup
import dev.inmo.tgbotapi.types.queries.callback.CallbackQuery
import dev.inmo.tgbotapi.utils.PreviewFeature
import kotlinx.serialization.*
import kotlinx.serialization.protobuf.ProtoBuf
import java.util.*
import kotlin.reflect.KClass

const val PREFIX = "PGR"

@OptIn(PreviewFeature::class)
class InlineKeyboardPager<Data : Any, BS : Any, BU : Any, U : BU>(
    private val id: String,
    private val limit: Int,
    private val dataKClass: KClass<Data>,
    private val block: PagerBuilder<Data, BS, BU, out BS, U>.() -> InlineKeyboardMarkup
) {
    fun replyMarkup(data: Data, context: BaseStatefulContext<BS, BU, *, U>?) = page(data, 0, context)

    context(BaseStatefulContext<BS, BU, *, U>)
    fun replyMarkup(data: Data) = replyMarkup(data, this@BaseStatefulContext)

    fun page(data: Data, index: Int, context: BaseStatefulContext<BS, BU, *, U>?) =
        block(PagerBuilder(index, index * limit, limit, data, dataKClass, id, context))

    context(BaseStatefulContext<BS, BU, *, U>)
    fun page(data: Data, index: Int) = page(data, index, this@BaseStatefulContext)

    @OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)
    internal fun RoleFilterBuilder<BS, BU, U, UserId>.setupTriggers() {
        anyState {
            add(
                Trigger<BS, BU, BS, U, Triple<Int, Data, CallbackQuery>>(
                    handler = { (page, data, query) ->
                        val message = query.asMessageCallbackQuery()?.message ?: return@Trigger
                        try {
                            editMessageReplyMarkup(message, page(data, page, this))
                        } catch (_: MessageIsNotModifiedException) {
                        }
                        answer(query)
                    }
                ) {
                    asCallbackQueryUpdate()?.data?.asDataCallbackQuery()
                        ?.takeIf { it.data.startsWith(PREFIX) }
                        ?.let {
                            runCatching {
                                val withoutPrefix = it.data.removePrefix(PREFIX)
                                check(withoutPrefix.startsWith(id))
                                val tokens = it.data.removePrefix(id).split(' ', limit = 2)
                                val page = tokens[0].toInt()
                                val rawData = tokens[1]
                                val data =
                                    ProtoBuf.decodeFromByteArray(dataKClass.serializer(), Base122.decode(rawData))
                                Triple(page, data, it)
                            }.getOrNull()
                        }
                }
            )
        }
    }
}

fun <BS : Any, BU : Any, U : BU> RoleFilterBuilder<BS, BU, U, UserId>.pager(
    id: String,
    limit: Int = 5,
    block: PagerBuilder<Unit, BS, BU, out BS, U>.() -> InlineKeyboardMarkup
) = pager(id, limit, Unit::class, block)

fun <BS : Any, BU : Any, U : BU, Data : Any> RoleFilterBuilder<BS, BU, U, UserId>.pager(
    id: String,
    limit: Int = 5,
    dataKClass: KClass<Data>,
    block: PagerBuilder<Data, BS, BU, out BS, U>.() -> InlineKeyboardMarkup
): InlineKeyboardPager<Data, BS, BU, U> {
    return InlineKeyboardPager(id, limit, dataKClass, block).also {
        with(it) { setupTriggers() }
    }
}

context(BaseStatefulContext<BS, BU, *, U>)
val <BS : Any, BU : Any, U : BU> InlineKeyboardPager<Unit, BS, BU, U>.replyMarkup
    get() = replyMarkup(Unit, context = this@BaseStatefulContext)
