package dev.slne.surf.player.core.client.minimap.tracker

import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class MapVisibilityTest {
    private val justJoined = 100.milliseconds
    private val settled = MapVisibility.VANISH_GRACE

    @Test
    fun `a player hidden in the world stays off the map`() {
        assertFalse(MapVisibility.isShown(canSee = false, onlineFor = settled) { error("not asked") })
    }

    @Test
    fun `a visible player is shown right after joining`() {
        assertTrue(MapVisibility.isShown(canSee = true, onlineFor = justJoined) { false })
    }

    @Test
    fun `a vanished player is shown to those who see them in the world`() {
        assertTrue(MapVisibility.isShown(canSee = true, onlineFor = settled) { true })
    }

    @Test
    fun `a vanished player is hidden from everyone right after joining`() {
        assertFalse(MapVisibility.isShown(canSee = true, onlineFor = justJoined) { true })
        assertFalse(MapVisibility.isShown(canSee = true, onlineFor = Duration.ZERO) { true })
    }
}
