package com.ithersta.tgbotapi.fsm

import dev.inmo.tgbotapi.bot.RequestsExecutor
import dev.inmo.tgbotapi.extensions.api.send.sendTextMessage
import dev.inmo.tgbotapi.extensions.utils.asBaseSentMessageUpdate
import dev.inmo.tgbotapi.extensions.utils.asCommonMessage
import dev.inmo.tgbotapi.extensions.utils.withContent
import dev.inmo.tgbotapi.types.BotCommand
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.update.abstracts.Update
import dev.inmo.tgbotapi.utils.PreviewFeature

@OptIn(PreviewFeature::class)
suspend fun RequestsExecutor.tryHandlingHelp(update: Update, getCommands: () -> List<BotCommand>): Boolean {
    val message = update.asBaseSentMessageUpdate()?.data?.asCommonMessage()?.withContent<TextContent>() ?: return false
    if (message.content.text != "/help") return false
    sendTextMessage(message.chat, getCommands().joinToString(separator = "\n") {
        "/${it.command} – ${it.description}"
    })
    return true
}
