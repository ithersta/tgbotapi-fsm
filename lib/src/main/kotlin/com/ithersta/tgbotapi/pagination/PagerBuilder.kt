package com.ithersta.tgbotapi.pagination

import com.ithersta.tgbotapi.encoder.Base122
import dev.inmo.tgbotapi.extensions.utils.types.buttons.InlineKeyboardBuilder
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.utils.row
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

open class PagerBuilder<Data : Any>(
    val page: Int,
    val offset: Int,
    val limit: Int,
    val data: Data,
    private val dataKClass: KClass<Data>,
    private val id: String
) {
    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
    fun InlineKeyboardBuilder.navigationRow(itemCount: Int, previous: String = "⬅️", next: String = "➡️") {
        val encodedData = Base122.encode(ProtoBuf.encodeToByteArray(dataKClass.serializer(), data))
        val maxPage = ((itemCount - 1) / limit).coerceAtLeast(0)
        fun goToPage(index: Int) = "$PREFIX$id${(index.coerceIn(0, maxPage))} $encodedData"
        if (maxPage == 0 && page == 0) return
        row {
            dataButton(if (page > 0) previous else " ", goToPage(page - 1))
            dataButton("${page + 1}/${maxPage + 1}", goToPage(page))
            dataButton(if (page < maxPage) next else " ", goToPage(page + 1))
        }
    }
}
