package com.ithersta.tgbotapi.fsm.entities.triggers

import com.ithersta.tgbotapi.fsm.builders.StateFilterBuilder
import dev.inmo.tgbotapi.extensions.utils.baseSentMessageUpdateOrNull
import dev.inmo.tgbotapi.extensions.utils.chatEventMessageOrNull
import dev.inmo.tgbotapi.types.message.ChatEvents.WebAppData
import dev.inmo.tgbotapi.types.message.ChatEvents.abstracts.ChatEvent
import dev.inmo.tgbotapi.types.message.PrivateEventMessage
import dev.inmo.tgbotapi.types.message.abstracts.ChatEventMessage

inline fun <BS : Any, BU : Any, S : BS, U : BU, K : Any, reified T : ChatEvent, reified CEM : ChatEventMessage<T>> StateFilterBuilder<BS, BU, S, U, K>.onEvent(
    noinline handler: Handler<BS, BU, S, U, CEM>,
    crossinline filter: (CEM) -> Boolean = { true }
) = add(
    Trigger(handler) {
        (baseSentMessageUpdateOrNull()?.data?.chatEventMessageOrNull()?.takeIf { it.chatEvent is T } as? CEM)
            ?.takeIf(filter)
    }
)

inline fun <BS : Any, BU : Any, S : BS, U : BU, K : Any> StateFilterBuilder<BS, BU, S, U, K>.onWebAppData(
    noinline handler: Handler<BS, BU, S, U, PrivateEventMessage<WebAppData>>,
    crossinline filter: (PrivateEventMessage<WebAppData>) -> Boolean = { true }
) = onEvent(handler, filter)
