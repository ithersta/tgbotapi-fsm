package com.ithersta.tgbotapi.persistence

import org.jetbrains.exposed.sql.Table

object UserStates : Table() {
    val userId = long("user_id")
    val sequenceNumber = long("sequence_number")
    val state = binary("state")
    override val primaryKey = PrimaryKey(userId, sequenceNumber)
}
