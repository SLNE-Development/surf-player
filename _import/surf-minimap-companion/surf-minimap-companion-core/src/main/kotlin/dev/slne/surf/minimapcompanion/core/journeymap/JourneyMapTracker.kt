package dev.slne.surf.minimapcompanion.core.journeymap

import dev.slne.surf.minimapcompanion.core.platform.CompanionPlayer
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Feeds JourneyMap's player radar while the server provides the players, as JourneyMap's server addon does.
 * JourneyMap then shows only these players, so every player in the viewer's dimension is listed and the ones
 * the viewer may not see are listed as hidden.
 *
 * Positions come in through [update]; each viewer is synced by its own [tick], so a platform can run both
 * on the thread that owns the player.
 */
class JourneyMapTracker {
    private val players = ConcurrentHashMap<UUID, JourneyMapPlayer>()
    private val listed = ConcurrentHashMap<UUID, Set<UUID>>()

    fun update(uuid: UUID, player: JourneyMapPlayer) {
        players[uuid] = player
    }

    /**
     * Sends [viewer] every other player in their dimension, with a position when [isVisible] lets them see the
     * player, and removes the players listed last time who are gone from the list.
     */
    fun tick(viewer: CompanionPlayer, isVisible: (target: UUID) -> Boolean) {
        if (!JourneyMapChannels.hasJourneyMap(viewer)) return
        val dimension = players[viewer.uuid]?.dimension ?: return

        val entries = LinkedHashMap<UUID, JourneyMapPlayer?>()
        for ((target, player) in players) {
            if (target == viewer.uuid || player.dimension != dimension) continue
            entries[target] = player.takeIf { isVisible(target) }
        }

        if (entries.isNotEmpty()) {
            viewer.sendPluginMessage(JourneyMapChannels.PLAYER_LOCATIONS, JourneyMapPackets.playerLocations(entries))
        }
        for (target in listed[viewer.uuid].orEmpty() - entries.keys) {
            viewer.sendPluginMessage(JourneyMapChannels.REMOVE_PLAYER, JourneyMapPackets.removePlayer(target))
        }
        listed[viewer.uuid] = entries.keys.toSet()
    }

    fun forget(uuid: UUID) {
        players.remove(uuid)
        listed.remove(uuid)
    }
}
