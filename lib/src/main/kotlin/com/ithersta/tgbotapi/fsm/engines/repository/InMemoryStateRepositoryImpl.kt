package com.ithersta.tgbotapi.fsm.engines.repository

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class InMemoryStateRepositoryImpl<K : Any, BS : Any>(
    private val historyDepth: Int
) : StateRepository<K, BS> {
    private val lock = ReentrantLock()
    private val states = hashMapOf<MapKey<K>, List<BS>>()
    private val sequenceNumbers = hashMapOf<K, Int>()

    override fun get(key: K): List<BS>? = lock.withLock {
        val sequenceNumber = sequenceNumbers[key] ?: return null
        return states[MapKey(key, sequenceNumber)]
    }

    override fun set(key: K, stateStack: List<BS>): Unit = lock.withLock {
        val sequenceNumber = sequenceNumbers[key]?.plus(1) ?: 0
        states[MapKey(key, sequenceNumber)] = stateStack
        sequenceNumbers[key] = sequenceNumber
        states.remove(MapKey(key, sequenceNumber - historyDepth))
    }

    override fun rollback(key: K): List<BS>? = lock.withLock {
        val sequenceNumber = sequenceNumbers[key]?.minus(1) ?: return null
        return states[MapKey(key, sequenceNumber)]?.also {
            sequenceNumbers[key] = sequenceNumber
        }
    }

    data class MapKey<K>(
        val key: K,
        val sequenceNumber: Int
    )
}
