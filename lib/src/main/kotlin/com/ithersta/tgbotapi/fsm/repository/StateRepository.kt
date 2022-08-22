package com.ithersta.tgbotapi.fsm.repository

import dev.inmo.tgbotapi.types.UserId

interface StateRepository<BaseState : Any> {
    fun get(userId: UserId): BaseState
    fun set(userId: UserId, state: BaseState)
}
