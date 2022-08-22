package com.ithersta.tgbotapi.fsm.repository

import dev.inmo.tgbotapi.types.UserId
import java.util.concurrent.ConcurrentHashMap

class InMemoryStateRepositoryImpl<BaseState : Any>(private val default: BaseState) : StateRepository<BaseState> {
    private val states = ConcurrentHashMap<UserId, BaseState>()

    override fun get(userId: UserId): BaseState {
        return states[userId] ?: default
    }

    override fun set(userId: UserId, state: BaseState) {
        states[userId] = state
    }
}
