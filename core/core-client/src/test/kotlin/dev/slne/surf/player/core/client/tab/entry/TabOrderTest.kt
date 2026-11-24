package dev.slne.surf.player.core.client.tab.entry

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TabOrderTest {

    @Test
    fun `visible players are ordered by their raw LuckPerms weight`() {
        assertEquals(0, TabOrder.of(0, vanished = false))
        assertEquals(100, TabOrder.of(100, vanished = false))
    }

    @Test
    fun `negative weights are passed through unchanged`() {
        assertEquals(-5, TabOrder.of(-5, vanished = false))
        assertEquals(-5 + TabOrder.VANISHED_ORDER_OFFSET, TabOrder.of(-5, vanished = true))
    }

    @Test
    fun `vanished players are listed above every visible player`() {
        val vanishedLowest = TabOrder.of(0, vanished = true)
        val visibleHighest = TabOrder.of(999_999, vanished = false)

        assertTrue(vanishedLowest > visibleHighest)
        assertEquals(1_000_100, TabOrder.of(100, vanished = true))
    }
}
