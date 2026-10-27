package dev.slne.surf.minimapcompanion.core.tracker

import dev.slne.surf.minimapcompanion.core.platform.CompanionPlayer
import dev.slne.surf.minimapcompanion.core.xaero.XaeroChannels
import dev.slne.surf.minimapcompanion.core.xaero.XaeroPackets
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Shows players other players on Xaero's maps beyond their view distance, as the mods' player tracker does.
 *
 * Positions come in through [update]; each viewer is synced by its own [tick], so a platform can run both
 * on the thread that owns the player.
 */
class PlayerTracker {
    private val positions = ConcurrentHashMap<UUID, Published>()
    private val sentPositions = ConcurrentHashMap<UUID, MutableMap<UUID, Published>>()

    /**
     * A target's position as viewers get it, encoded once for all of them. Viewers compare it by identity.
     */
    private class Published(uuid: UUID, val position: TrackedPosition) {
        val payload = XaeroPackets.trackedPlayer(uuid, position.x, position.y, position.z, position.dimension)
        val removal = XaeroPackets.trackedPlayerRemoved(uuid)
    }

    /**
     * Records where the player with [uuid] is. Moves under [MIN_MOVE] blocks within a dimension keep the position
     * viewers already have, since the maps show tracked players as icons.
     */
    fun update(uuid: UUID, position: TrackedPosition) {
        val current = positions[uuid]
        if (current != null && current.position.isNear(position)) return
        positions[uuid] = Published(uuid, position)
    }

    /**
     * Sends [viewer] the [candidates] [isShown] lets them see that are new or moved, and removes the ones they no
     * longer see. `null` candidates are every tracked player.
     */
    fun tick(viewer: CompanionPlayer, candidates: Collection<UUID>?, isShown: (target: UUID) -> Boolean) {
        val sent = sentPositions[viewer.uuid]
        if (candidates != null && candidates.isEmpty() && sent.isNullOrEmpty()) return

        val channel = trackerChannel(viewer) ?: return
        val viewerSent = sent ?: sentPositions.computeIfAbsent(viewer.uuid) { ConcurrentHashMap() }
        val tracked = HashSet<UUID>()

        for (target in candidates ?: positions.keys) {
            if (target == viewer.uuid) continue
            val published = positions[target] ?: continue
            if (!isShown(target)) continue

            tracked += target
            if (viewerSent.put(target, published) !== published) {
                viewer.sendPluginMessage(channel, published.payload)
            }
        }

        val iterator = viewerSent.entries.iterator()
        while (iterator.hasNext()) {
            val (target, published) = iterator.next()
            if (target in tracked) continue

            iterator.remove()
            viewer.sendPluginMessage(channel, published.removal)
        }
    }

    /**
     * Clears the tracked players [viewer]'s client knows, including ones another server sent.
     */
    fun reset(viewer: CompanionPlayer) {
        sentPositions.remove(viewer.uuid)
        for (channel in XaeroChannels.MAPS) {
            if (viewer.hasChannel(channel)) {
                viewer.sendPluginMessage(channel, XaeroPackets.trackerReset())
            }
        }
    }

    fun forget(uuid: UUID) {
        positions.remove(uuid)
        sentPositions.remove(uuid)
    }

    companion object {
        /**
         * How far a player must move before viewers get the new position.
         */
        const val MIN_MOVE = 1.0

        /**
         * The channel the mods expect tracked players on: the minimap's when the client has it, as the world map
         * then reads them from the minimap.
         */
        fun trackerChannel(player: CompanionPlayer) = when {
            player.hasChannel(XaeroChannels.MINIMAP) -> XaeroChannels.MINIMAP
            player.hasChannel(XaeroChannels.WORLD_MAP) -> XaeroChannels.WORLD_MAP
            else -> null
        }

        private fun TrackedPosition.isNear(other: TrackedPosition): Boolean {
            if (dimension != other.dimension) return false
            val dx = x - other.x
            val dy = y - other.y
            val dz = z - other.z
            return dx * dx + dy * dy + dz * dz < MIN_MOVE * MIN_MOVE
        }
    }
}
