package dev.slne.surf.minimapcompanion.core.journeymap

import dev.slne.surf.minimapcompanion.core.service.FakePlayer
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class JourneyMapTrackerTest {
    private val overworld = "minecraft:overworld"
    private val nether = "minecraft:the_nether"

    private val viewer = FakePlayer(channels = setOf(JourneyMapChannels.PERMISSIONS, JourneyMapChannels.PLAYER_LOCATIONS))
    private val a = UUID.randomUUID()
    private val b = UUID.randomUUID()

    private val viewerPosition = JourneyMapPlayer(1, 0.0, 64.0, 0.0, 0f, 0f, overworld)
    private val posA = JourneyMapPlayer(2, 10.0, 64.0, 10.0, 90f, 0f, overworld)
    private val posB = JourneyMapPlayer(3, 5.0, 40.0, 5.0, 0f, 0f, nether)

    private val tracker = JourneyMapTracker().apply {
        update(viewer.uuid, viewerPosition)
        update(a, posA)
        update(b, posB)
    }

    private fun locations(players: Map<UUID, JourneyMapPlayer?>) =
        JourneyMapChannels.PLAYER_LOCATIONS to JourneyMapPackets.playerLocations(players).toList()

    private fun removed(uuid: UUID) = JourneyMapChannels.REMOVE_PLAYER to JourneyMapPackets.removePlayer(uuid).toList()

    @Test
    fun `lists every other player in the viewer's dimension`() {
        tracker.tick(viewer) { true }
        assertEquals(listOf(locations(mapOf(a to posA))), viewer.sentContent)
    }

    @Test
    fun `hidden players are listed without a position`() {
        tracker.tick(viewer) { false }
        assertEquals(listOf(locations(mapOf(a to null))), viewer.sentContent)
    }

    @Test
    fun `players who leave the viewer's dimension are removed`() {
        tracker.tick(viewer) { true }
        tracker.update(a, posA.copy(dimension = nether))
        viewer.sent.clear()

        tracker.tick(viewer) { true }
        assertEquals(listOf(removed(a)), viewer.sentContent)
    }

    @Test
    fun `viewer changing worlds removes the old world's players`() {
        tracker.tick(viewer) { true }
        tracker.update(viewer.uuid, viewerPosition.copy(dimension = nether))
        viewer.sent.clear()

        tracker.tick(viewer) { true }
        assertEquals(setOf(locations(mapOf(b to posB)), removed(a)), viewer.sentContent.toSet())
    }

    @Test
    fun `forgotten players are removed from other viewers`() {
        tracker.tick(viewer) { true }
        tracker.forget(a)
        viewer.sent.clear()

        tracker.tick(viewer) { true }
        assertEquals(listOf(removed(a)), viewer.sentContent)
    }

    @Test
    fun `viewer without journeymap gets nothing`() {
        val xaeroOnly = FakePlayer()
        tracker.update(xaeroOnly.uuid, viewerPosition)
        tracker.tick(xaeroOnly) { true }
        assertEquals(emptyList(), xaeroOnly.sent)
    }

    @Test
    fun `viewer without a position gets nothing`() {
        val newcomer = FakePlayer(channels = setOf(JourneyMapChannels.PERMISSIONS))
        tracker.tick(newcomer) { true }
        assertEquals(emptyList(), newcomer.sent)
    }
}
