package dev.slne.surf.player.core.client.minimap.journeymap

import dev.slne.surf.player.core.client.minimap.service.FakePlayer
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
        val movedA = posA.copy(x = 11.0)
        tracker.update(a, movedA)
        tickShowingAll()
        assertEquals(listOf(locations(mapOf(a to movedA))), viewer.sentContent)
    }

    @Test
    fun `an unchanged listing is not resent`() {
        tickedOnce()
        tickShowingAll()
        assertEquals(emptyList(), viewer.sent)
    }

    @Test
    fun `an unchanged listing is resent after the keep-alive`() {
        tickedOnce()
        repeat(JourneyMapTracker.KEEP_ALIVE_RUNS - 1) { tickShowingAll() }
        assertEquals(emptyList(), viewer.sent)

        tickShowingAll()
        assertEquals(listOf(locations(mapOf(a to posA))), viewer.sentContent)
    }

    @Test
    fun `a player who only turned is resent`() {
        tickedOnce()
        val turnedA = posA.copy(yaw = 180f)
        tracker.update(a, turnedA)
        tickShowingAll()
        assertEquals(listOf(locations(mapOf(a to turnedA))), viewer.sentContent)
    }

    @Test
    fun `only the players that changed are resent`() {
        val c = UUID.randomUUID()
        val posC = JourneyMapPlayer(4, -20.0, 70.0, 3.0, 0f, 0f, overworld)
        tracker.update(c, posC)
        tickedOnce()

        val movedA = posA.copy(x = 11.0)
        tracker.update(a, movedA)
        tickShowingAll()
        assertEquals(listOf(locations(mapOf(a to movedA))), viewer.sentContent)
    }

    @Test
    fun `the keep-alive lists every player, not only the changed ones`() {
        val c = UUID.randomUUID()
        val posC = JourneyMapPlayer(4, -20.0, 70.0, 3.0, 0f, 0f, overworld)
        tracker.update(c, posC)
        tickedOnce()

        val movedA = posA.copy(x = 11.0)
        tracker.update(a, movedA)
        tickShowingAll()
        repeat(JourneyMapTracker.KEEP_ALIVE_RUNS - 1) { tickShowingAll() }

        val keepAlive = viewer.sentContent.last()
        assertEquals(JourneyMapChannels.PLAYER_LOCATIONS, keepAlive.first)
        assertEquals(
            setOf(locations(mapOf(a to movedA, c to posC)), locations(mapOf(c to posC, a to movedA))).contains(keepAlive),
            true,
        )
    }

    @Test
    fun `a small turn is not resent`() {
        tickedOnce()
        tracker.update(a, posA.copy(yaw = posA.yaw + 5f))
        tickShowingAll()
        assertEquals(emptyList(), viewer.sent)
    }

    @Test
    fun `looking up or down is not resent`() {
        tickedOnce()
        tracker.update(a, posA.copy(pitch = 60f))
        tickShowingAll()
        assertEquals(emptyList(), viewer.sent)
    }

    @Test
    fun `turning across north counts the short way round`() {
        tracker.update(a, posA.copy(yaw = 358f))
        tickedOnce()
        tracker.update(a, posA.copy(yaw = 2f))
        tickShowingAll()
        assertEquals(emptyList(), viewer.sent)
    }

    @Test
    fun `a viewer sees the players whose client tracks them, not the other way round`() {
        // a's client tracks the viewer, but the viewer's client does not track a.
        tracker.update(viewer.uuid, viewerPosition, trackedBy = setOf(a))
        tracker.update(a, posA, trackedBy = emptySet())

        assertFalse(tracker.sees(viewer.uuid, a))
        assertTrue(tracker.sees(a, viewer.uuid))
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
