package dev.slne.surf.minimapcompanion.core.tracker

import dev.slne.surf.minimapcompanion.core.service.FakePlayer
import dev.slne.surf.minimapcompanion.core.xaero.XaeroChannels
import dev.slne.surf.minimapcompanion.core.xaero.XaeroPackets
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class PlayerTrackerTest {
    private val tracker = PlayerTracker()
    private val viewer = FakePlayer()
    private val friend = UUID.randomUUID()
    private val position = TrackedPosition(1.0, 64.0, -2.0, "minecraft:overworld")

    // By default the viewer is related to the friend only and may see everyone.
    private fun tick(
        viewer: FakePlayer = this.viewer,
        candidates: Collection<UUID>? = setOf(friend),
        isShown: (UUID) -> Boolean = { true },
    ) = tracker.tick(viewer, candidates, isShown)

    private fun added(uuid: UUID, at: TrackedPosition, channel: String = XaeroChannels.MINIMAP) =
        channel to XaeroPackets.trackedPlayer(uuid, at.x, at.y, at.z, at.dimension).toList()

    private fun removed(uuid: UUID) = XaeroChannels.MINIMAP to XaeroPackets.trackedPlayerRemoved(uuid).toList()

    @Test
    fun `newly tracked player is sent to the viewer`() {
        tracker.update(friend, position)
        tick()
        assertEquals(listOf(added(friend, position)), viewer.sentContent)
    }

    @Test
    fun `only candidates are sent`() {
        tracker.update(friend, position)
        tracker.update(UUID.randomUUID(), position)
        tick()
        assertEquals(listOf(added(friend, position)), viewer.sentContent)
    }

    @Test
    fun `without candidates every tracked player is considered`() {
        val stranger = UUID.randomUUID()
        tracker.update(friend, position)
        tracker.update(stranger, position)
        tick(candidates = null)
        assertEquals(setOf(added(friend, position), added(stranger, position)), viewer.sentContent.toSet())
    }

    @Test
    fun `candidates the viewer may not see are not sent`() {
        tracker.update(friend, position)
        tick(isShown = { false })
        assertEquals(emptyList(), viewer.sent)
    }

    @Test
    fun `unchanged position is not resent`() {
        tracker.update(friend, position)
        tick()
        tick()
        assertEquals(1, viewer.sent.size)
    }

    @Test
    fun `moved player is resent`() {
        val moved = position.copy(x = 5.0)
        tracker.update(friend, position)
        tick()
        tracker.update(friend, moved)
        tick()
        assertEquals(listOf(added(friend, position), added(friend, moved)), viewer.sentContent)
    }

    @Test
    fun `moving less than a block is not resent`() {
        tracker.update(friend, position)
        tick()
        tracker.update(friend, position.copy(x = position.x + 0.5, z = position.z + 0.5))
        tick()
        assertEquals(listOf(added(friend, position)), viewer.sentContent)
    }

    @Test
    fun `moving a block is resent with the new position`() {
        val moved = position.copy(y = position.y + 1.0)
        tracker.update(friend, position)
        tick()
        tracker.update(friend, moved)
        tick()
        assertEquals(listOf(added(friend, position), added(friend, moved)), viewer.sentContent)
    }

    @Test
    fun `changing dimension is resent at the same coordinates`() {
        val nether = position.copy(dimension = "minecraft:the_nether")
        tracker.update(friend, position)
        tick()
        tracker.update(friend, nether)
        tick()
        assertEquals(listOf(added(friend, position), added(friend, nether)), viewer.sentContent)
    }

    @Test
    fun `a target is encoded once for every viewer`() {
        val other = FakePlayer()
        tracker.update(friend, position)
        tick()
        tick(viewer = other)
        assertSame(viewer.sent.single().second, other.sent.single().second)
    }

    @Test
    fun `a position is only encoded once a viewer gets it`() {
        var encoded = 0
        val tracker = PlayerTracker { uuid, at ->
            encoded++
            XaeroPackets.trackedPlayer(uuid, at.x, at.y, at.z, at.dimension)
        }

        tracker.update(friend, position)
        tracker.update(friend, position.copy(x = 5.0))
        assertEquals(0, encoded)

        tracker.tick(viewer, setOf(friend)) { true }
        tracker.tick(FakePlayer(), setOf(friend)) { true }
        assertEquals(1, encoded)
    }

    @Test
    fun `a viewer has sent players until they are all removed`() {
        assertFalse(tracker.hasSent(viewer.uuid))

        tracker.update(friend, position)
        tick()
        assertTrue(tracker.hasSent(viewer.uuid))

        tick(candidates = emptySet())
        assertFalse(tracker.hasSent(viewer.uuid))
    }

    @Test
    fun `player no longer tracked is removed`() {
        tracker.update(friend, position)
        tick()
        tick(candidates = emptySet())
        assertEquals(listOf(added(friend, position), removed(friend)), viewer.sentContent)
    }

    @Test
    fun `player the viewer may no longer see is removed`() {
        tracker.update(friend, position)
        tick()
        tick(isShown = { false })
        assertEquals(listOf(added(friend, position), removed(friend)), viewer.sentContent)
    }

    @Test
    fun `player who left is removed`() {
        tracker.update(friend, position)
        tick()
        tracker.forget(friend)
        tick()
        assertEquals(listOf(added(friend, position), removed(friend)), viewer.sentContent)
    }

    @Test
    fun `viewer never tracks itself`() {
        tracker.update(viewer.uuid, position)
        tick(candidates = null)
        tick(candidates = setOf(viewer.uuid))
        assertEquals(emptyList(), viewer.sent)
    }

    @Test
    fun `viewer without map channels receives nothing`() {
        val viewer = FakePlayer(channels = setOf(XaeroChannels.LIB))
        tracker.update(friend, position)
        tick(viewer = viewer)
        assertEquals(emptyList(), viewer.sent)
    }

    @Test
    fun `tracker prefers the minimap channel`() {
        val worldMapOnly = FakePlayer(channels = setOf(XaeroChannels.WORLD_MAP))
        assertEquals(XaeroChannels.MINIMAP, PlayerTracker.trackerChannel(viewer))
        assertEquals(XaeroChannels.WORLD_MAP, PlayerTracker.trackerChannel(worldMapOnly))

        tracker.update(friend, position)
        tick(viewer = worldMapOnly)
        assertEquals(listOf(added(friend, position, XaeroChannels.WORLD_MAP)), worldMapOnly.sentContent)
    }

    @Test
    fun `reset clears the state and sends a reset on each map channel`() {
        tracker.update(friend, position)
        tick()
        tracker.reset(viewer)
        tick()
        assertEquals(
            listOf(
                added(friend, position),
                XaeroChannels.WORLD_MAP to XaeroPackets.trackerReset().toList(),
                XaeroChannels.MINIMAP to XaeroPackets.trackerReset().toList(),
                added(friend, position),
            ),
            viewer.sentContent,
        )
    }
}
