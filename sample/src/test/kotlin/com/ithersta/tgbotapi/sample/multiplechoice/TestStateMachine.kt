package com.ithersta.tgbotapi.sample.multiplechoice

import com.ithersta.tgbotapi.test.receiveDataQuery
import com.ithersta.tgbotapi.test.receiveTextMessage
import com.ithersta.tgbotapi.test.test
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TestStateMachine {
    @Test
    fun test() = stateMachine.test(
        user = Unit,
        initialState = EmptyState
    ) {
        assertEquals(EmptyState, state)
        receiveTextMessage("/start")
        assertEquals(MultipleChoiceState(emptySet(), 0L), state)
        receiveDataQuery(SelectQuery(Clothes.Hat))
        assertEquals(MultipleChoiceState(setOf(Clothes.Hat), 0L), state)
    }
}


