package com.ithersta.tgbotapi.test

import dev.inmo.tgbotapi.types.message.content.TextMessage
import dev.inmo.tgbotapi.types.update.MessageUpdate
import io.mockk.every
import io.mockk.mockk

suspend fun TestEngine<*, *, *>.receiveTextMessage(text: String) {
    val textMessage = mockk<TextMessage>()
    every { textMessage.content.text } returns text
    every { textMessage.messageId } returns 0L
    dispatch(MessageUpdate(0L, textMessage))
}
