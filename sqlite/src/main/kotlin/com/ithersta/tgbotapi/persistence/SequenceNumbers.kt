package com.ithersta.tgbotapi.persistence

import org.jetbrains.exposed.dao.id.LongIdTable

object SequenceNumbers : LongIdTable() {
    val sequenceNumber = long("sequence_number")
}
