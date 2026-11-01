package app.simplecloud.plugin.signs.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SignShineTest {

    @Test
    fun `sweeps the highlight from the first to the last character`() {
        val start = SignShine.intensities(7, 0.1)
        val end = SignShine.intensities(7, 0.5)

        assertTrue(start.first() > start.last())
        assertTrue(end.last() > end.first())
    }

    @Test
    fun `pauses without a highlight after the sweep`() {
        assertEquals(List(7) { 0f }, SignShine.intensities(7, 0.0))
        assertEquals(List(7) { 0f }, SignShine.intensities(7, 0.8))
    }
}
