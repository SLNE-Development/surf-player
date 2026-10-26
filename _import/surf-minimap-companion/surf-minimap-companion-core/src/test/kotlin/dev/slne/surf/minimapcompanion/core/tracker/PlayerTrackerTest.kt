package dev.slne.surf.minimapcompanion.core.tracker

import dev.slne.surf.minimapcompanion.core.service.FakePlayer
import dev.slne.surf.minimapcompanion.core.xaero.XaeroChannels
import dev.slne.surf.minimapcompanion.core.xaero.XaeroPackets
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class PlayerTrackerTest {
    private val tracker = PlayerTracker()
    private val viewer = FakePlayer()
    private val friend = UUID.randomUUID()
    private val position = TrackedPosition(1.0, 64.0, -2.0, "minecraft:overworld")

    private val tracksFriend: (UUID, UUID) -> Boolean = { _, target -> target == friend }

    private fun added(uuid: UUID, at: TrackedPosition, channel: String = XaeroChannels.MINIMAP) =
        channel to XaeroPackets.trackedPlayer(uuid, at.x, at.y, at.z, at.dimension).toList()

    private fun removed(uuid: UUID) = XaeroChannels.MINIMAP to XaeroPackets.trackedPlayerRemoved(uuid).toList()

    @Test
    fun `newly tracked player is sent to the viewer`() {
        tracker.update(friend, position)
        tracker.tick(viewer, tracksFriend)
        assertEquals(listOf(added(friend, position)), viewer.sentContent)
    }

    @Test
    fun `untracked players are not sent`() {
        tracker.update(friend, position)
        tracker.update(UUID.randomUUID(), position)
        tracker.tick(viewer, tracksFriend)
        assertEquals(listOf(added(friend, position)), viewer.sentContent)
    }

    @Test
    fun `unchanged position is not resent`() {
        tracker.update(friend, position)
        tracker.tick(viewer, tracksFriend)
        tracker.tick(viewer, tracksFriend)
        assertEquals(1, viewer.sent.size)
    }

    @Test
    fun `moved player is resent`() {
        val moved = position.copy(x = 5.0)
        tracker.update(friend, position)
        tracker.tick(viewer, tracksFriend)
        tracker.update(friend, moved)
        tracker.tick(viewer, tracksFriend)
        assertEquals(listOf(added(friend, position), added(friend, moved)), viewer.sentContent)
    }

    @Test
    fun `player no longer tracked is removed`() {
        tracker.update(friend, position)
        tracker.tick(viewer, tracksFriend)
        tracker.tick(viewer) { _, _ -> false }
        assertEquals(listOf(added(friend, position), removed(friend)), viewer.sentContent)
    }

    @Test
    fun `player who left is removed`() {
        tracker.update(friend, position)
        tracker.tick(viewer, tracksFriend)
        tracker.forget(friend)
        tracker.tick(viewer, tracksFriend)
        assertEquals(listOf(added(friend, position), removed(friend)), viewer.sentContent)
    }

    @Test
    fun `viewer never tracks itself`() {
        tracker.update(viewer.uuid, position)
        tracker.tick(viewer) { _, _ -> true }
        assertEquals(emptyList(), viewer.sent)
    }

    @Test
    fun `viewer without map channels receives nothing`() {
        val viewer = FakePlayer(channels = setOf(XaeroChannels.LIB))
        tracker.update(friend, position)
        tracker.tick(viewer, tracksFriend)
        assertEquals(emptyList(), viewer.sent)
    }

    @Test
    fun `tracker prefers the minimap channel`() {
        val worldMapOnly = FakePlayer(channels = setOf(XaeroChannels.WORLD_MAP))
        assertEquals(XaeroChannels.MINIMAP, PlayerTracker.trackerChannel(viewer))
        assertEquals(XaeroChannels.WORLD_MAP, PlayerTracker.trackerChannel(worldMapOnly))

        tracker.update(friend, position)
        tracker.tick(worldMapOnly, tracksFriend)
        assertEquals(listOf(added(friend, position, XaeroChannels.WORLD_MAP)), worldMapOnly.sentContent)
    }

    @Test
    fun `reset clears the state and sends a reset on each map channel`() {
        tracker.update(friend, position)
        tracker.tick(viewer, tracksFriend)
        tracker.reset(viewer)
        tracker.tick(viewer, tracksFriend)
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
