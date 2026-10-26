package dev.slne.surf.minimapcompanion.core.journeymap

import dev.slne.surf.minimapcompanion.core.platform.CompanionPlayer
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Feeds JourneyMap's player radar while the server provides the players, as JourneyMap's server addon does.
 * JourneyMap then shows only the players listed here, so each viewer gets the players the tracker shows and the
 * ones their client sees nearby. Players the viewer may not see are never sent, not even as hidden entries.
 *
 * Positions come in through [update]; each viewer is synced by its own [tick], so a platform can run both
 * on the thread that owns the player.
 */
class JourneyMapTracker {
    private val players = ConcurrentHashMap<UUID, JourneyMapPlayer>()
    private val concealed: MutableSet<UUID> = ConcurrentHashMap.newKeySet()
    private val listed = ConcurrentHashMap<UUID, Listing>()

    private data class Listing(val dimension: String, val players: Set<UUID>)

    /**
     * Records where the player with [uuid] is. A [concealed] player is sneaking, invisible or spectating, whom
     * JourneyMap's own radar would leave out.
     */
    fun update(uuid: UUID, player: JourneyMapPlayer, concealed: Boolean = false) {
        players[uuid] = player
        if (concealed) this.concealed += uuid else this.concealed -= uuid
    }

    /**
     * Sends [viewer] the players in their dimension that [isTracked] shows them, and the ones [isNearby] says their
     * client sees unless those are concealed. Both checks must already leave out players the viewer may not see.
     */
    fun tick(viewer: CompanionPlayer, isTracked: (target: UUID) -> Boolean, isNearby: (target: UUID) -> Boolean) {
        if (!JourneyMapChannels.hasJourneyMap(viewer)) return
        val dimension = players[viewer.uuid]?.dimension ?: return

        val visible = LinkedHashMap<UUID, JourneyMapPlayer?>()
        for ((target, player) in players) {
            if (target == viewer.uuid || player.dimension != dimension) continue
            if (isTracked(target) || (isNearby(target) && target !in concealed)) visible[target] = player
        }

        // The client keeps every listed player in the world it first saw them in and drops them from the radar
        // once the viewer is elsewhere. So only players listed in the same world before are kept; everyone else
        // is removed, and new ones are cleared first, before the list arrives.
        val previous = listed[viewer.uuid]
        val kept = if (previous?.dimension == dimension) previous.players intersect visible.keys else emptySet()
        for (target in previous?.players.orEmpty() + visible.keys - kept) {
            viewer.sendPluginMessage(JourneyMapChannels.REMOVE_PLAYER, JourneyMapPackets.removePlayer(target))
        }
        if (visible.isNotEmpty()) {
            viewer.sendPluginMessage(JourneyMapChannels.PLAYER_LOCATIONS, JourneyMapPackets.playerLocations(visible))
        }
        listed[viewer.uuid] = Listing(dimension, visible.keys.toSet())
    }

    fun forget(uuid: UUID) {
        players.remove(uuid)
        concealed.remove(uuid)
        listed.remove(uuid)
    }
}
