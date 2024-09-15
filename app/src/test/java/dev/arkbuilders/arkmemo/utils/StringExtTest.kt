package dev.arkbuilders.arkmemo.utils

import org.junit.Assert.*

import org.junit.Test

class StringExtTest {

    @Test
    fun `insert string in the middle of another string`() {
        val result = "Hello World".insertStringAtPosition("Beautiful ", 6)
        assertEquals("Hello Beautiful World", result)
    }

    @Test
    fun `insert string at the beginning`() {
        val result = "World".insertStringAtPosition("Hello ", 0)
        assertEquals("Hello World", result)
    }

    @Test
    fun `insert string at the end`() {
        val result = "Hello".insertStringAtPosition(" World", 5)
        assertEquals("Hello World", result)
    }

    @Test
    fun `insert string into an empty string`() {
        val result = "".insertStringAtPosition("Hello", 0)
        assertEquals("Hello", result)
    }

    @Test
    fun `insert empty string at any position`() {
        val result = "Hello World".insertStringAtPosition("", 5)
        assertEquals("Hello World", result)
    }

    @Test
    fun `insert string at out-of-bound negative position throws exception`() {
        assertThrows(IndexOutOfBoundsException::class.java) {
            "Hello World".insertStringAtPosition("Test", -1)
        }
    }

    @Test
    fun `insert string at out-of-bound position greater than length throws exception`() {
        assertThrows(IndexOutOfBoundsException::class.java) {
            "Hello".insertStringAtPosition("World", 10)
        }
    }

    @Test
    fun `insert string into a string with special characters`() {
        val result = "Hello @#$%!".insertStringAtPosition("Beautiful ", 6)
        assertEquals("Hello Beautiful @#$%!", result)
    }

    @Test
    fun `insert string into a single-character string`() {
        val result = "A".insertStringAtPosition("B", 1)
        assertEquals("AB", result)
    }

    @Test
    fun `insert string with newline characters`() {
        val result = "Hello\nWorld".insertStringAtPosition(" Beautiful", 5)
        assertEquals("Hello Beautiful\nWorld", result)
    }

}