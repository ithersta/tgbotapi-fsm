package com.ithersta.tgbotapi.test

import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.requests.abstracts.Request
import dev.inmo.tgbotapi.types.message.content.TextMessage
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk

val mockTelegramBot = mockk<TelegramBot>(relaxed = true).apply {
    mockTextMessageRequest()
}

private fun TelegramBot.mockTextMessageRequest() {
    val textMessage = mockk<TextMessage>()
    every { textMessage.messageId } returns 0L
    coEvery { execute(any<Request<TextMessage>>()) } returns textMessage
}
