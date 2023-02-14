package com.ithersta.tgbotapi.sample.menu

import kotlinx.serialization.Serializable

object MenuStates {
    @Serializable
    object Main : DialogState

    @Serializable
    object OptionOne : DialogState
}
