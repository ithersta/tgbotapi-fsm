package com.ithersta.tgbotapi.fsm.repository

import java.util.concurrent.ConcurrentHashMap

class InMemoryStateRepositoryImpl<K : Any, BS : Any>(private val default: BS) : StateRepository<K, BS> {
    private val states = ConcurrentHashMap<K, BS>()

    override fun get(key: K): BS {
        return states[key] ?: default
    }

    override fun set(key: K, state: BS) {
        states[key] = state
    }
}
