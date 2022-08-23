package com.ithersta.tgbotapi.fsm.repository

interface StateRepository<Key : Any, BaseState : Any> {
    fun get(key: Key): BaseState
    fun set(key: Key, state: BaseState)
}
