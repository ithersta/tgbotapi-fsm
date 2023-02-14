package com.ithersta.tgbotapi.sample.menu
 import kotlinx.serialization.Serializable

interface DialogState {
    @Serializable
    object Empty : DialogState
}
