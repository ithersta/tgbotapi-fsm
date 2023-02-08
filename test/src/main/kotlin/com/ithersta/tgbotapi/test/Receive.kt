package com.ithersta.tgbotapi.test

import com.ithersta.tgbotapi.fsm.entities.triggers.Base64Encoder
import dev.inmo.tgbotapi.extensions.utils.types.buttons.dataButton
import dev.inmo.tgbotapi.types.message.content.TextMessage
import dev.inmo.tgbotapi.types.queries.callback.DataCallbackQuery
import dev.inmo.tgbotapi.types.update.CallbackQueryUpdate
import dev.inmo.tgbotapi.types.update.MessageUpdate
import dev.inmo.tgbotapi.types.update.abstracts.Update
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.serializer

suspend fun TestEngine<*, *, *>.receiveTextMessage(text: String) {
    val textMessage = mockk<TextMessage>()
    every { textMessage.content.text } returns text
    every { textMessage.messageId } returns 0L
    dispatch(MessageUpdate(0L, textMessage))
}

@OptIn(ExperimentalSerializationApi::class)
suspend inline fun <reified Q : Any> TestEngine<*, *, *>.receiveDataQuery(data: Q) {
    val baseType = Q::class.supertypes.first()
    val serializer = serializer(baseType)
    val byteArray = ProtoBuf.encodeToByteArray(serializer, data)
    val dataString = Base64Encoder.encodeToString(byteArray)
    val dataQuery = mockk<DataCallbackQuery>()
    every { dataQuery.data } returns dataString
    every { dataQuery.id } returns ""
    dispatch(CallbackQueryUpdate(0L, dataQuery))
}
