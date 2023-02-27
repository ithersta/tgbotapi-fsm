package com.ithersta.tgbotapi.pagination

import com.ithersta.tgbotapi.encoder.Base122
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
class InlineKeyboardPager<Data : Any>(
    private val id: String,
    private val limit: Int,
    private val dataKClass: KClass<Data>,
    private val block: PagerBuilder<Data>.() -> InlineKeyboardMarkup
) {
    fun replyMarkup(data: Data) = page(data, 0)
    fun page(data: Data, index: Int) = block(PagerBuilder(index, index * limit, limit, data, dataKClass, id))

    @OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)
    internal fun <BS : Any, BU : Any, U : BU> RoleFilterBuilder<BS, BU, U, UserId>.setupTriggers() {
        anyState {
            add(
                Trigger<_, _, _, _, Triple<Int, Data, CallbackQuery>>(
                    handler = { (page, data, query) ->
                        val message = query.asMessageCallbackQuery()?.message ?: return@Trigger
                        try {
                            editMessageReplyMarkup(message, page(data, page))
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
                                val data = ProtoBuf.decodeFromByteArray(dataKClass.serializer(), Base122.decode(rawData))
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
    block: PagerBuilder<Unit>.() -> InlineKeyboardMarkup
) = pager(id, limit, Unit::class, block)

fun <BS : Any, BU : Any, U : BU, Data : Any> RoleFilterBuilder<BS, BU, U, UserId>.pager(
    id: String,
    limit: Int = 5,
    dataKClass: KClass<Data>,
    block: PagerBuilder<Data>.() -> InlineKeyboardMarkup
): InlineKeyboardPager<Data> {
    return InlineKeyboardPager(id, limit, dataKClass, block).also {
        with(it) { setupTriggers() }
    }
}

val InlineKeyboardPager<Unit>.replyMarkup get() = replyMarkup(Unit)
