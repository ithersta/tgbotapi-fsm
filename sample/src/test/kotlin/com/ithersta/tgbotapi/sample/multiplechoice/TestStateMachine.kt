package com.ithersta.tgbotapi.sample.multiplechoice

import com.ithersta.tgbotapi.test.receiveTextMessage
import com.ithersta.tgbotapi.test.test
import org.junit.jupiter.api.Test

class TestStateMachine {
    @Test
    fun test() = stateMachine.test(
        user = Unit,
        initialState = EmptyState
    ) {
        assert(state is EmptyState)
        receiveTextMessage("/start")
        assert(state is MultipleChoiceState)
    }
}


