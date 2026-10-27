package dev.slne.surf.minimapcompanion.core.journeymap

import dev.slne.surf.minimapcompanion.core.platform.CompanionPlayer
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

/**
 * Feeds JourneyMap's player radar while the server provides the players, as JourneyMap's server addon does.
 * JourneyMap then shows only the players listed here, so each viewer gets the players the tracker shows and the
 * ones their client sees nearby. Players the viewer may not see are never sent, not even as hidden entries.
 *
 * Positions come in through [update]; each viewer is synced by its own [tick], so a platform can run both
 * on the thread that owns the player.
 */
class JourneyMapTracker {
    private val players = ConcurrentHashMap<UUID, Entry>()
    private val concealed: MutableSet<UUID> = ConcurrentHashMap.newKeySet()
    private val trackers = ConcurrentHashMap<UUID, Set<UUID>>()
    private val listed = ConcurrentHashMap<UUID, Listing>()

    /**
     * A player as viewers get them, encoded once for all of them. Viewers compare it by identity.
     */
    private class Entry(uuid: UUID, val player: JourneyMapPlayer) {
        val bytes = JourneyMapPackets.playerEntry(uuid, player)
    }

    private class Listing(val dimension: String, val entries: Map<UUID, Entry>, val runsSinceSend: Int)

    /**
     * Records where the player with [uuid] is. A [concealed] player is sneaking, invisible or spectating, whom
     * JourneyMap's own radar would leave out. [trackedBy] are the players whose clients see this player, read on
     * this player's own thread.
     */
    fun update(uuid: UUID, player: JourneyMapPlayer, concealed: Boolean = false, trackedBy: Set<UUID> = emptySet()) {
        val current = players[uuid]
        if (current == null || !current.player.looksLike(player)) players[uuid] = Entry(uuid, player)
        if (concealed) this.concealed += uuid else this.concealed -= uuid
        trackers[uuid] = trackedBy
    }

    /**
     * Whether [viewer]'s client sees [target] in the world, as [target]'s last [update] said. Tracking is not always
     * mutual: a player with a short view distance can be seen by others without seeing them.
     */
    fun sees(viewer: UUID, target: UUID) = trackers[target]?.contains(viewer) == true

    /**
     * Sends [viewer] the players in their dimension that [isTracked] shows them, and the ones [isNearby] says their
     * client sees unless those are concealed. Both checks must already leave out players the viewer may not see.
     */
    fun tick(viewer: CompanionPlayer, isTracked: (target: UUID) -> Boolean, isNearby: (target: UUID) -> Boolean) {
        if (!JourneyMapChannels.hasJourneyMap(viewer)) return
        val dimension = players[viewer.uuid]?.player?.dimension ?: return

        val visible = LinkedHashMap<UUID, Entry>()
        for ((target, entry) in players) {
            if (target == viewer.uuid || entry.player.dimension != dimension) continue
            if (isTracked(target) || (isNearby(target) && target !in concealed)) visible[target] = entry
        }

        // The client keeps every listed player in the world it first saw them in and drops them from the radar
        // once the viewer is elsewhere. So only players listed in the same world before are kept; everyone else
        // is removed, and new ones are cleared first, before the list arrives.
        val previous = listed[viewer.uuid]?.takeIf { it.dimension == dimension }
        val kept = previous?.entries?.keys.orEmpty() intersect visible.keys
        for (target in listed[viewer.uuid]?.entries?.keys.orEmpty() + visible.keys - kept) {
            viewer.sendPluginMessage(JourneyMapChannels.REMOVE_PLAYER, JourneyMapPackets.removePlayer(target))
        }

        // The client keeps listed players until they are removed, so the list only goes out when an entry changed,
        // and now and then in case the client skipped a player it did not know from the tab list yet.
        val changed = previous == null || visible.any { (target, entry) -> previous.entries[target] !== entry }
        val keepAlive = previous != null && previous.runsSinceSend + 1 >= KEEP_ALIVE_RUNS
        val send = visible.isNotEmpty() && (changed || keepAlive)
        if (send) {
            val locations = JourneyMapPackets.playerLocations(visible.values.map(Entry::bytes))
            viewer.sendPluginMessage(JourneyMapChannels.PLAYER_LOCATIONS, locations)
        }
        listed[viewer.uuid] = Listing(dimension, visible, if (send || previous == null) 0 else previous.runsSinceSend + 1)
    }

    fun forget(uuid: UUID) {
        players.remove(uuid)
        concealed.remove(uuid)
        trackers.remove(uuid)
        listed.remove(uuid)
    }

    companion object {
        /**
         * After how many runs without a change a viewer gets the list again: 2 seconds at the Paper tracker's 5 ticks.
         */
        const val KEEP_ALIVE_RUNS = 8

        /**
         * How far a player must turn before viewers get their new heading. The radar draws players as small arrows.
         */
        const val MIN_TURN_DEGREES = 10f

        // The radar shows where a player is and which way they face; looking up or down shows nowhere.
        private fun JourneyMapPlayer.looksLike(other: JourneyMapPlayer): Boolean {
            if (entityId != other.entityId || x != other.x || y != other.y || z != other.z || dimension != other.dimension) {
                return false
            }
            val turn = ((yaw - other.yaw) % 360f + 540f) % 360f - 180f
            return abs(turn) < MIN_TURN_DEGREES
        }
    }
}
