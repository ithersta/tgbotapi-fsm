package com.ithersta.tgbotapi.fsm.repository

import java.util.concurrent.ConcurrentHashMap

class InMemoryStateRepositoryImpl<Key : Any, BaseState : Any>(private val default: BaseState) :
    StateRepository<Key, BaseState> {
    private val states = ConcurrentHashMap<Key, BaseState>()

    override fun get(key: Key): BaseState {
        return states[key] ?: default
    }

    override fun set(key: Key, state: BaseState) {
        states[key] = state
    }
}
