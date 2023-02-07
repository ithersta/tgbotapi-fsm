package com.ithersta.tgbotapi.sample.multiplechoice

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface Query

@Serializable
@SerialName("s")
class SelectQuery(val clothes: Clothes) : Query

@Serializable
@SerialName("u")
class UnselectQuery(val clothes: Clothes) : Query
