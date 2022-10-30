package com.ithersta.tgbotapi.persistence

import org.jetbrains.exposed.dao.id.LongIdTable

object UserStates : LongIdTable() {
    val state = binary("state")
}
