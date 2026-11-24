package dev.slne.surf.player.paper.vanish.util

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*

class SpectateHotkeysTest {
    private val player = UUID.randomUUID()
    private val hotkeys = SpectateHotkeys(doubleSneakWindowMillis = 500)

    private fun press(at: Long): Boolean {
        val double = hotkeys.onInput(player, shift = true, nowMillis = at)
        hotkeys.onInput(player, shift = false, nowMillis = at + 10)
        return double
    }

    @Test
    fun `two presses within the window are a double sneak`() {
        assertFalse(press(1_000))
        assertTrue(press(1_150))
    }

    @Test
    fun `two presses too far apart are no double sneak`() {
        assertFalse(press(1_000))
        assertFalse(press(1_600))
    }

    @Test
    fun `holding sneak while other input changes is no second press`() {
        assertFalse(hotkeys.onInput(player, shift = true, nowMillis = 1_000))
        assertFalse(hotkeys.onInput(player, shift = true, nowMillis = 1_100))
        assertFalse(hotkeys.onInput(player, shift = true, nowMillis = 1_200))
    }

    @Test
    fun `a double sneak starts counting anew`() {
        assertFalse(press(1_000))
        assertTrue(press(1_150))
        assertFalse(press(1_300))
    }

    @Test
    fun `back needs the sneak key held, not a recent press`() {
        assertTrue(!press(1_000) && !hotkeys.isSneaking(player))

        hotkeys.onInput(player, shift = true, nowMillis = 2_000)
        assertTrue(hotkeys.isSneaking(player))

        hotkeys.onInput(player, shift = false, nowMillis = 2_200)
        assertFalse(hotkeys.isSneaking(player))
    }

    @Test
    fun `a forgotten player starts without state`() {
        hotkeys.onInput(player, shift = true, nowMillis = 1_000)
        hotkeys.forget(player)

        assertFalse(hotkeys.isSneaking(player))
        assertFalse(press(1_100))
    }
}
