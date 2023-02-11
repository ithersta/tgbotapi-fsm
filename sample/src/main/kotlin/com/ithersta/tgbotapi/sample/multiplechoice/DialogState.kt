package com.ithersta.tgbotapi.sample.multiplechoice

import dev.inmo.tgbotapi.types.MessageId
import kotlinx.serialization.Serializable

interface DialogState

@Serializable
object EmptyState : DialogState

@Serializable
data class MultipleChoiceState(
    val selectedClothes: Set<Clothes> = emptySet(),
    val messageId: MessageId? = null
) : DialogState
