package dev.slne.surf.player.core.client.vanish

import dev.slne.surf.player.core.client.vanish.permission.VanishPermissions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class VanishSightTest {
    private fun priority(n: Any) = "${VanishPermissions.VANISH_PRIORITY}.$n"

    @Test
    fun `the explicit priority is the highest numeric priority node`() {
        assertEquals(10, VanishSight.explicitPriority(sequenceOf(priority(2), priority(10), priority(3))))
        assertEquals(1, VanishSight.explicitPriority(sequenceOf(priority(1), priority("abc"), "other.node")))
        assertNull(VanishSight.explicitPriority(sequenceOf("surf.player.vanish.prioritylist", priority("x"))))
        assertNull(VanishSight.explicitPriority(emptySequence()))
    }

    @Test
    fun `the viewer priority falls back to 0 for team members and none for everyone else`() {
        val team = { permission: String -> permission == VanishPermissions.TEAM_VANISH }

        assertEquals(4, VanishSight.viewerPriority(sequenceOf(priority(4)), team))
        assertEquals(0, VanishSight.viewerPriority(emptySequence(), team))
        assertEquals(4, VanishSight.viewerPriority(sequenceOf(priority(4))) { false })
        assertNull(VanishSight.viewerPriority(emptySequence()) { false })
    }

    @Test
    fun `the target priority defaults to 0`() {
        assertEquals(3, VanishSight.targetPriority(sequenceOf(priority(3))))
        assertEquals(0, VanishSight.targetPriority(emptySequence()))
    }

    @Test
    fun `equal, higher and lower priorities`() {
        assertTrue(VanishSight.canSee(false, false, viewerPriority = 5, targetPriority = 5))
        assertTrue(VanishSight.canSee(false, false, viewerPriority = 6, targetPriority = 5))
        assertFalse(VanishSight.canSee(false, false, viewerPriority = 4, targetPriority = 5))
        assertFalse(VanishSight.canSee(false, false, viewerPriority = null, targetPriority = 0))
    }

    @Test
    fun `bypass and self always see`() {
        assertTrue(VanishSight.canSee(false, viewerHasBypass = true, viewerPriority = null, targetPriority = 100))
        assertTrue(VanishSight.canSee(viewerIsTarget = true, viewerHasBypass = false, viewerPriority = null, targetPriority = 100))
    }

    @Test
    fun `a normal player without vanish permissions sees no vanished player`() {
        val priority = VanishSight.effectivePriority(explicitPriority = null, mayVanish = false)

        assertNull(priority)
        assertFalse(VanishSight.canSee(false, viewerHasBypass = false, viewerPriority = priority, targetPriority = null))
    }

    @Test
    fun `staff who may vanish see vanished players without a priority`() {
        val priority = VanishSight.effectivePriority(explicitPriority = null, mayVanish = true)

        assertEquals(0, priority)
        assertTrue(VanishSight.canSee(false, viewerHasBypass = false, viewerPriority = priority, targetPriority = null))
        assertFalse(VanishSight.canSee(false, viewerHasBypass = false, viewerPriority = priority, targetPriority = 1))
    }

    @Test
    fun `an explicit priority wins over the staff default`() {
        assertEquals(7, VanishSight.effectivePriority(explicitPriority = 7, mayVanish = true))
        assertEquals(7, VanishSight.effectivePriority(explicitPriority = 7, mayVanish = false))
    }
}
