package com.ithersta.tgbotapi.test

import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.requests.abstracts.Request
import dev.inmo.tgbotapi.types.message.abstracts.Message
import dev.inmo.tgbotapi.types.message.content.TextMessage
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk

val mockTelegramBot = run {
    val telegramBot = mockk<TelegramBot>(relaxed = true)
    val textMessage = mockk<TextMessage>()
    every { textMessage.messageId } returns 0L
    coEvery { telegramBot.execute(any<Request<Message>>()) } returns textMessage
    telegramBot
}
