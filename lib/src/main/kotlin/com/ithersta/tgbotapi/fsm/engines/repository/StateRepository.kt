package com.ithersta.tgbotapi.fsm.engines.repository

interface StateRepository<Key : Any, BaseState : Any> {
    fun get(key: Key): List<BaseState>?
    fun set(key: Key, stateStack: List<BaseState>)
    fun rollback(key: Key): List<BaseState>?
}
