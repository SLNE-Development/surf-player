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

    private fun tickShowingAll() = tracker.tick(viewer, isTracked = { true }, isNearby = { false })

    // The first listing of a player is cleared first; most tests only look at what follows.
    private fun tickedOnce() = apply {
        tickShowingAll()
        viewer.sent.clear()
    }

    @Test
    fun `newly listed players are cleared first, so the client builds them in its current world`() {
        tickShowingAll()
        assertEquals(listOf(removed(a), locations(mapOf(a to posA))), viewer.sentContent)
    }

    @Test
    fun `players listed before are listed again without being cleared`() {
        tickedOnce()
        tickShowingAll()
        assertEquals(listOf(locations(mapOf(a to posA))), viewer.sentContent)
    }

    @Test
    fun `players the client sees nearby are listed`() {
        tracker.tick(viewer, isTracked = { false }, isNearby = { true })
        assertEquals(locations(mapOf(a to posA)), viewer.sentContent.last())
    }

    @Test
    fun `sneaking, invisible or spectating players nearby stay off the radar`() {
        tracker.update(a, posA, concealed = true)
        tracker.tick(viewer, isTracked = { false }, isNearby = { true })
        assertEquals(emptyList(), viewer.sent)
    }

    @Test
    fun `concealed players the tracker shows stay on the radar`() {
        tracker.update(a, posA, concealed = true)
        tracker.tick(viewer, isTracked = { true }, isNearby = { false })
        assertEquals(locations(mapOf(a to posA)), viewer.sentContent.last())
    }

    @Test
    fun `players the viewer may not see are never sent`() {
        tracker.tick(viewer, isTracked = { false }, isNearby = { false })
        assertEquals(emptyList(), viewer.sent)
    }

    @Test
    fun `players who become hidden are removed`() {
        tickedOnce()
        tracker.tick(viewer, isTracked = { false }, isNearby = { false })
        assertEquals(listOf(removed(a)), viewer.sentContent)
    }

    @Test
    fun `players who leave the viewer's dimension are removed`() {
        tickedOnce()
        tracker.update(a, posA.copy(dimension = nether))
        tickShowingAll()
        assertEquals(listOf(removed(a)), viewer.sentContent)
    }

    @Test
    fun `players who change worlds with the viewer are cleared before they are listed again`() {
        tickedOnce()
        val movedA = posA.copy(dimension = nether)
        tracker.update(viewer.uuid, viewerPosition.copy(dimension = nether))
        tracker.update(a, movedA)

        tracker.tick(viewer, isTracked = { it == a }, isNearby = { false })
        assertEquals(listOf(removed(a), locations(mapOf(a to movedA))), viewer.sentContent)
    }

    @Test
    fun `viewer changing worlds removes the old world's players before listing the new one's`() {
        tickedOnce()
        tracker.update(viewer.uuid, viewerPosition.copy(dimension = nether))
        tickShowingAll()

        val sent = viewer.sentContent
        assertEquals(setOf(removed(a), removed(b)), sent.dropLast(1).toSet())
        assertEquals(locations(mapOf(b to posB)), sent.last())
    }

    @Test
    fun `forgotten players are removed from other viewers`() {
        tickedOnce()
        tracker.forget(a)
        tickShowingAll()
        assertEquals(listOf(removed(a)), viewer.sentContent)
    }

    @Test
    fun `viewer without journeymap gets nothing`() {
        val xaeroOnly = FakePlayer()
        tracker.update(xaeroOnly.uuid, viewerPosition)
        tracker.tick(xaeroOnly, isTracked = { true }, isNearby = { true })
        assertEquals(emptyList(), xaeroOnly.sent)
    }

    @Test
    fun `viewer without a position gets nothing`() {
        val newcomer = FakePlayer(channels = setOf(JourneyMapChannels.PERMISSIONS))
        tracker.tick(newcomer, isTracked = { true }, isNearby = { true })
        assertEquals(emptyList(), newcomer.sent)
    }
}
