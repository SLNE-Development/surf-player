package dev.slne.surf.player.core.client.chat.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*

class IgnoreRulesTest {
    private val player = UUID.randomUUID()
    private val target = UUID.randomUUID()

    @Test
    fun `an ignore blocks messages of normal players`() {
        assertTrue(IgnoreRules.blocksMessage(ignoresSender = true) { false })
    }

    @Test
    fun `an ignore does not block messages of team members`() {
        assertFalse(IgnoreRules.blocksMessage(ignoresSender = true) { true })
    }

    @Test
    fun `without an ignore nothing is blocked and the exemption is not asked`() {
        assertFalse(IgnoreRules.blocksMessage(ignoresSender = false) { error("must not be asked") })
    }

    @Test
    fun `team members cannot be ignored`() {
        assertEquals(
            "Du kannst Keviro nicht ignorieren.",
            IgnoreRules.ignoreRefusal(player, target, "Keviro", alreadyIgnored = false) { true }
        )
    }

    @Test
    fun `normal players can be ignored`() {
        assertNull(IgnoreRules.ignoreRefusal(player, target, "Keviro", alreadyIgnored = false) { false })
    }

    @Test
    fun `ignoring yourself or twice is refused before the exemption is asked`() {
        assertEquals(
            "Du kannst dich nicht selbst ignorieren.",
            IgnoreRules.ignoreRefusal(player, player, "Ich", alreadyIgnored = false) { error("must not be asked") }
        )
        assertEquals(
            "Du ignorierst Keviro bereits.",
            IgnoreRules.ignoreRefusal(player, target, "Keviro", alreadyIgnored = true) { error("must not be asked") }
        )
    }
}
