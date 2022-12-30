package com.ithersta.tgbotapi.fsm.repository

interface StateRepository<Key : Any, BaseState : Any> {
    fun get(key: Key): List<BaseState>?
    fun set(key: Key, stateStack: List<BaseState>)
    fun rollback(key: Key): List<BaseState>?
}
