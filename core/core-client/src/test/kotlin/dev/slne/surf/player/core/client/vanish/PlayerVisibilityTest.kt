package dev.slne.surf.player.core.client.vanish

import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.player.core.client.vanish.permission.VanishPermissions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*

class PlayerVisibilityTest {
    private val visible = player("Alice")
    private val vanished = player("Vanessa")
    private val online = listOf(visible, vanished)

    /** Vanessa is vanished with priority 5. */
    private val visibility = PlayerVisibility { if (it == vanished.uuid) 5 else null }

    private val normalSender = CommandViewer(UUID.randomUUID(), { false })
    private val bypassSender = CommandViewer(UUID.randomUUID(), { it == VanishPermissions.VANISH_BYPASS })
    private val vanishedSelf = CommandViewer(vanished.uuid, { false })

    private fun staff(priority: Int?) = CommandViewer(
        UUID.randomUUID(),
        { it == VanishPermissions.TEAM_VANISH },
        { listOfNotNull(priority?.let { "${VanishPermissions.VANISH_PRIORITY}.$it" }).asSequence() },
    )

    @Test
    fun `a viewer with an equal priority sees the vanished player`() {
        assertTrue(visibility.canSee(staff(5), vanished.uuid))
    }

    @Test
    fun `a viewer with a higher priority sees the vanished player`() {
        assertTrue(visibility.canSee(staff(7), vanished.uuid))
        assertEquals(listOf("Alice", "Vanessa"), visibility.onlinePlayerNames(staff(7), online = online))
    }

    @Test
    fun `a viewer with a lower priority does not see the vanished player`() {
        assertFalse(visibility.canSee(staff(4), vanished.uuid))
        assertFalse(visibility.canSee(staff(null), vanished.uuid)) // team member without node: priority 0
        assertEquals(listOf("Alice"), visibility.onlinePlayerNames(staff(4), online = online))
    }

    @Test
    fun `a priority node without the team permission still counts`() {
        val viewer = CommandViewer(UUID.randomUUID(), { false }, { sequenceOf("${VanishPermissions.VANISH_PRIORITY}.9") })
        assertTrue(visibility.canSee(viewer, vanished.uuid))
    }

    @Test
    fun `everyone sees a player who is not vanished`() {
        assertTrue(visibility.canSee(normalSender, visible.uuid))
        assertTrue(visibility.canSee(staff(0), visible.uuid))
    }

    @Test
    fun `a team member without a priority node sees players vanished with priority 0`() {
        val lowVisibility = PlayerVisibility { if (it == vanished.uuid) 0 else null }
        assertTrue(lowVisibility.canSee(staff(null), vanished.uuid))
        assertFalse(lowVisibility.canSee(normalSender, vanished.uuid))
    }

    @Test
    fun `a vanished player is hidden from a normal sender`() {
        assertFalse(visibility.canSee(normalSender, vanished.uuid))
        assertTrue(visibility.canSee(normalSender, visible.uuid))

        assertEquals(listOf(visible), visibility.onlinePlayers(normalSender, online))
        assertEquals(listOf("Alice"), visibility.onlinePlayerNames(normalSender, online = online))
    }

    @Test
    fun `a sender with the bypass permission sees vanished players`() {
        assertTrue(visibility.canSee(bypassSender, vanished.uuid))
        assertEquals(listOf("Alice", "Vanessa"), visibility.onlinePlayerNames(bypassSender, online = online))
    }

    @Test
    fun `a vanished player sees themself`() {
        assertTrue(visibility.canSee(vanishedSelf, vanished.uuid))
        assertEquals(listOf("Alice", "Vanessa"), visibility.onlinePlayerNames(vanishedSelf, online = online))
    }

    @Test
    fun `the console sees vanished players`() {
        assertTrue(visibility.canSee(CommandViewer.CONSOLE, vanished.uuid))
    }

    @Test
    fun `names are filtered by the typed prefix ignoring case`() {
        assertEquals(listOf("Vanessa"), visibility.onlinePlayerNames(bypassSender, "va", online))
        assertEquals(emptyList<String>(), visibility.onlinePlayerNames(normalSender, "va", online))
    }

    @Test
    fun `resolving a hidden player by name behaves like an offline player`() {
        val lookup = { name: String -> online.firstOrNull { it.lastKnownName.equals(name, ignoreCase = true) } }

        assertNull(visibility.findOnlinePlayer(normalSender, "vanessa", lookup))
        assertNull(visibility.findOnlinePlayer(normalSender, "Bob", lookup))
        assertSame(visible, visibility.findOnlinePlayer(normalSender, "alice", lookup))
        assertSame(vanished, visibility.findOnlinePlayer(bypassSender, "vanessa", lookup))
        assertSame(vanished, visibility.findOnlinePlayer(vanishedSelf, "Vanessa", lookup))

        val byUuid = { uuid: UUID -> online.firstOrNull { it.uuid == uuid } }
        assertNull(visibility.findOnlinePlayer(normalSender, vanished.uuid, byUuid))
        assertSame(vanished, visibility.findOnlinePlayer(bypassSender, vanished.uuid, byUuid))
    }

    @Test
    fun `offline suggestions list a hidden player only among the offline names`() {
        val offlineNames = listOf("Aaron", "Vanessa", "Zoe")
        val byPrefix = { prefix: String, limit: Int ->
            offlineNames.filter { it.startsWith(prefix, ignoreCase = true) }.take(limit)
        }

        assertEquals(
            listOf("Alice", "Aaron", "Vanessa", "Zoe"),
            visibility.offlinePlayerNames(normalSender, "", online = online, offlineNamesByPrefix = byPrefix)
        )
        assertEquals(
            listOf("Alice", "Vanessa", "Aaron", "Zoe"),
            visibility.offlinePlayerNames(bypassSender, "", online = online, offlineNamesByPrefix = byPrefix)
        )
        assertEquals(
            listOf("Vanessa"),
            visibility.offlinePlayerNames(normalSender, "v", online = online, offlineNamesByPrefix = byPrefix)
        )
    }

    @Test
    fun `offline suggestions respect the limit`() {
        val byPrefix = { _: String, limit: Int -> listOf("Aaron", "Zoe").take(limit) }

        assertEquals(
            listOf("Alice", "Aaron"),
            visibility.offlinePlayerNames(normalSender, "", limit = 2, online = online, offlineNamesByPrefix = byPrefix)
        )
    }

    private fun player(name: String) = SurfPlayer(
        uuid = UUID.randomUUID(),
        lastKnownName = name,
        firstSeen = null,
        lastSeen = null,
        transferred = false,
    )
}
