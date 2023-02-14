package com.ithersta.tgbotapi.sample.pagination

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface Query

@Serializable
data class SampleQuery(val number: Int) : Query
