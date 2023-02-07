package com.ithersta.tgbotapi.sample.basic

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface Query

@Serializable
@SerialName("q")
class TestQuery(val name: String) : Query
