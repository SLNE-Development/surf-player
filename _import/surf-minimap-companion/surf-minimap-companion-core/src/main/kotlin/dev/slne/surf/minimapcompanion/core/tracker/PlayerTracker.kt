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
    private val positions = ConcurrentHashMap<UUID, TrackedPosition>()
    private val sentPositions = ConcurrentHashMap<UUID, MutableMap<UUID, TrackedPosition>>()

    fun update(uuid: UUID, position: TrackedPosition) {
        positions[uuid] = position
    }

    /**
     * Sends [viewer] the players [isTracked] lets them see that are new or moved, and removes the ones they no longer see.
     */
    fun tick(viewer: CompanionPlayer, isTracked: (viewer: UUID, target: UUID) -> Boolean) {
        val channel = trackerChannel(viewer) ?: return
        val sent = sentPositions.computeIfAbsent(viewer.uuid) { ConcurrentHashMap() }
        val tracked = HashSet<UUID>()

        for ((target, position) in positions) {
            if (target == viewer.uuid || !isTracked(viewer.uuid, target)) continue

            tracked += target
            if (sent.put(target, position) != position) {
                viewer.sendPluginMessage(
                    channel,
                    XaeroPackets.trackedPlayer(target, position.x, position.y, position.z, position.dimension)
                )
            }
        }

        val iterator = sent.keys.iterator()
        while (iterator.hasNext()) {
            val target = iterator.next()
            if (target in tracked) continue

            iterator.remove()
            viewer.sendPluginMessage(channel, XaeroPackets.trackedPlayerRemoved(target))
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
         * The channel the mods expect tracked players on: the minimap's when the client has it, as the world map
         * then reads them from the minimap.
         */
        fun trackerChannel(player: CompanionPlayer) = when {
            player.hasChannel(XaeroChannels.MINIMAP) -> XaeroChannels.MINIMAP
            player.hasChannel(XaeroChannels.WORLD_MAP) -> XaeroChannels.WORLD_MAP
            else -> null
        }
    }
}
