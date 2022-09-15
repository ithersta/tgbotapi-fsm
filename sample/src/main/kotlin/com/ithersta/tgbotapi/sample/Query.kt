package com.ithersta.tgbotapi.sample

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface Query

@Serializable
@SerialName("TestQuery")
class TestQuery(val name: String) : Query
