package com.ithersta.tgbotapi.sample.helloworld

import kotlinx.serialization.Serializable

@Serializable
sealed interface DialogState

@Serializable
object EmptyState : DialogState

@Serializable
class CounterState(val number: Int = 0) : DialogState
