package com.ithersta.tgbotapi.boot.encoder

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertTrue

class Base122Test {
    @Test
    fun test() {
        val averageSize = generateSequence {
            val byteArray = Random.nextBytes(64)
            val str = Base122.encode(byteArray)
            val newByteArray = Base122.decode(str)
            assertTrue(byteArray.contentEquals(newByteArray))
            str.toByteArray(Charsets.UTF_8).size
        }.take(1000000).average()
        println(averageSize)
    }
}
