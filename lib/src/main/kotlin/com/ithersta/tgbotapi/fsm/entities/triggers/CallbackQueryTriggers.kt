package com.ithersta.tgbotapi.fsm.entities.triggers

import com.ithersta.tgbotapi.fsm.builders.StateFilterBuilder
import dev.inmo.tgbotapi.extensions.utils.asCallbackQueryUpdate
import dev.inmo.tgbotapi.extensions.utils.asDataCallbackQuery
import dev.inmo.tgbotapi.types.queries.callback.DataCallbackQuery
import dev.inmo.tgbotapi.utils.PreviewFeature
import kotlinx.serialization.*
import java.util.*

@OptIn(PreviewFeature::class)
fun <BS : Any, BU : Any, S : BS, U : BU, K : Any> StateFilterBuilder<BS, BU, S, U, K>.onDataCallbackQuery(
    regex: Regex, filter: (DataCallbackQuery) -> Boolean = { true }, handler: Handler<BS, BU, S, U, DataCallbackQuery>
) = add(Trigger(handler) {
    asCallbackQueryUpdate()?.data?.asDataCallbackQuery()
        ?.takeIf(filter)
        ?.takeIf { regex.matches(it.data) }
})
