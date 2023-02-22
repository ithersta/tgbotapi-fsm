package com.ithersta.tgbotapi.sample.statefulpagination

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface Query

@Serializable
@SerialName("s")
data class SampleQuery(val number: Int) : Query
