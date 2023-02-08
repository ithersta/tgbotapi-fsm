package com.ithersta.tgbotapi.sample.multiplechoice

import com.ithersta.tgbotapi.test.receiveDataQuery
import com.ithersta.tgbotapi.test.receiveTextMessage
import com.ithersta.tgbotapi.test.test
import org.junit.jupiter.api.Test

class TestStateMachine {
    @Test
    fun test() = stateMachine.test(
        user = Unit,
        initialState = EmptyState
    ) {
        assert(state == EmptyState)
        receiveTextMessage("/start")
        assert((state as MultipleChoiceState).selectedClothes == emptySet<Clothes>())
        receiveDataQuery(SelectQuery(Clothes.Hat))
        assert((state as MultipleChoiceState).selectedClothes == setOf(Clothes.Hat))
    }
}


