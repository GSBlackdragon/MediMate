package com.example.mms.Utils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class HashKtTest {

    @Test
    fun hashStringTest() {
        val testString = "pokpoqmoif<dsmfl,,ef"
        assertNotEquals(testString, hashString(testString))
    }
}