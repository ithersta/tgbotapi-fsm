package com.ithersta.tgbotapi.fsm.repository

import java.util.concurrent.ConcurrentHashMap

class InMemoryStateRepositoryImpl<K : Any, BS : Any>() : StateRepository<K, BS> {
    private val states = ConcurrentHashMap<K, List<BS>>()

    override fun get(key: K): List<BS>? {
        return states[key]
    }

    override fun set(key: K, stateStack: List<BS>) {
        states[key] = stateStack
    }
}
