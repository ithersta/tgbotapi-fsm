package com.ithersta.tgbotapi.sample.helloworld

import com.ithersta.tgbotapi.test.receiveTextMessage
import com.ithersta.tgbotapi.test.test
import kotlin.test.Test
import kotlin.test.assertEquals

class TestStateMachine {
    @Test
    fun test() = stateMachine.test(user = Unit, initialState = EmptyState) {
        receiveTextMessage("/counter")
        assertEquals(CounterState(0), state)
        receiveTextMessage("+")
        assertEquals(CounterState(1), state)
        receiveTextMessage("+")
        assertEquals(CounterState(2), state)
        receiveTextMessage("-")
        assertEquals(CounterState(1), state)
        receiveTextMessage("-")
        assertEquals(CounterState(0), state)
        receiveTextMessage("-")
        assertEquals(CounterState(-1), state)
    }
}
