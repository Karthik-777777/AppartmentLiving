package com.simats.appartmentliving

import org.junit.Test
import org.junit.Assert.*
import com.simats.appartmentliving.ui.screens.capitalizeWords
import com.simats.appartmentliving.ui.screens.getInitials

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun getInitials_isCorrect() {
        assertEquals("RS", getInitials("Rahul Sharma"))
        assertEquals("AK", getInitials("Amit Kumar Gupta"))
        assertEquals("S", getInitials("Suresh"))
        assertEquals("RS", getInitials(""))
    }

    @Test
    fun capitalizeWords_isCorrect() {
        assertEquals("Leakage Problem", "leakage problem".capitalizeWords())
        assertEquals("Water Leakage", "WATER LEAKAGE".capitalizeWords())
        assertEquals("Plumbing Fix Needed", "  plumbing   fix   needed  ".capitalizeWords())
    }
}