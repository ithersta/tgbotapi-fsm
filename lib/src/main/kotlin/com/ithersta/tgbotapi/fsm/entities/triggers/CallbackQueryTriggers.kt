package com.ithersta.tgbotapi.fsm.entities.triggers

import com.ithersta.tgbotapi.fsm.builders.StateFilterBuilder
import dev.inmo.tgbotapi.extensions.utils.asCallbackQueryUpdate
import dev.inmo.tgbotapi.extensions.utils.asDataCallbackQuery
import dev.inmo.tgbotapi.types.queries.callback.DataCallbackQuery
import dev.inmo.tgbotapi.utils.PreviewFeature

@OptIn(PreviewFeature::class)
fun <BaseState : Any, S : BaseState, Key : Any> StateFilterBuilder<BaseState, S, Key>.onDataCallbackQuery(
    regex: Regex,
    handler: Handler<BaseState, S, DataCallbackQuery>,
    filter: (DataCallbackQuery) -> Boolean = { true }
) = add(
    Trigger(handler) {
        asCallbackQueryUpdate()?.data?.asDataCallbackQuery()
            ?.takeIf(filter)
            ?.takeIf { regex.matches(it.data) }
    }
)
