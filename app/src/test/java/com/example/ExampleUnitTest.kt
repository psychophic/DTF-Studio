package com.example

import com.example.engine.DtfEngine
import com.example.model.DtfParams
import com.example.model.PrintConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun defaultParams_areCorrect() {
        val params = DtfParams()
        assertEquals("Remove Background", params.mode)
        assertEquals("Black shirt", params.target)
        assertEquals("Dot", params.shape)
        assertEquals(10f, params.dot)
        assertEquals(45f, params.angle)
        assertEquals(300, params.dpi.toInt())
    }

    @Test
    fun fitBox_calculatesCorrectAspect() {
        val (w, h) = DtfEngine.fitBox(1000, 500, 200, 200)
        assertEquals(200, w)
        assertEquals(100, h)
    }

    @Test
    fun printSizes_defaultContainsEssentialSizes() {
        val sizes = PrintConstants.defaultSizes()
        assertNotNull(sizes.firstOrNull { it.name.contains("Center Chest") })
        assertNotNull(sizes.firstOrNull { it.name.contains("Left Chest") })
    }
}
