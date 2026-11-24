package dev.slne.surf.player.paper.minimap.tracker

import com.github.shynixn.mccoroutine.folia.asyncDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.api.core.util.logger
import dev.slne.surf.player.core.client.minimap.command.MapTrackerInfo
import dev.slne.surf.player.core.client.minimap.command.MapTrackerMode
import dev.slne.surf.player.core.client.minimap.config.CompanionConfigs
import dev.slne.surf.player.core.client.minimap.journeymap.JourneyMapPlayer
import dev.slne.surf.player.core.client.minimap.journeymap.JourneyMapTracker
import dev.slne.surf.player.core.client.minimap.tracker.MapVisibility
import dev.slne.surf.player.core.client.minimap.tracker.PlayerTracker
import dev.slne.surf.player.core.client.minimap.tracker.RelationSource
import dev.slne.surf.player.core.client.minimap.tracker.TrackedPosition
import dev.slne.surf.player.core.client.minimap.tracker.TrackerRelations
import dev.slne.surf.player.paper.minimap.hook.ClanHook
import dev.slne.surf.player.paper.minimap.hook.FriendsHook
import dev.slne.surf.player.paper.minimap.platform.PaperCompanionPlayer
import dev.slne.surf.player.paper.plugin
import dev.slne.surf.player.paper.vanish.util.isVanished
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

/**
 * Runs Xaero's player tracker on Paper and Folia. Every player syncs on their own scheduler, so positions
 * are only read on the thread that owns the player.
 */
object PaperTracker {
    private const val TICK_INTERVAL = 5L
    private val RELATION_REFRESH_INTERVAL = 10.seconds

    private val log = logger()
    private val tracker = PlayerTracker()
    private val journeyMapTracker = JourneyMapTracker()

    @Volatile
    private var everyoneTracksEveryone = false

    // JourneyMap shows only server-sent players while any switch is on, matching the permissions MapSyncService sends.
    @Volatile
    private var journeyMapRadar = false

    @Volatile
    private var relations: TrackerRelations? = null

    @Volatile
    private var relationRefresh: Job? = null

    private val joinedAt = ConcurrentHashMap<UUID, TimeSource.Monotonic.ValueTimeMark>()

    /**
     * Reads the tracker config and starts refreshing the relations of the enabled sources.
     * Called again on reload, off the players' threads; players stop or start seeing each other on their next tick.
     * Sources still enabled keep their relations, so their players stay on the map meanwhile.
     */
    @Synchronized
    fun configure() {
        val config = CompanionConfigs.current.config.tracker
        everyoneTracksEveryone = config.everyoneTracksEveryone
        journeyMapRadar = config.isEnabled

        val sources = buildList<RelationSource> {
            if (config.friends) add(FriendsHook)
            if (config.clanMembers) add(ClanHook)
        }

        // Built in full before it replaces the old relations, so a tick on another thread sees either, never none.
        val next = if (sources.isEmpty()) null else TrackerRelations(sources, relations).also { next ->
            for (player in Bukkit.getOnlinePlayers()) {
                next.add(player.uniqueId)
            }
        }
        relations = next

        relationRefresh?.cancel()
        relationRefresh = next?.let { refreshed ->
            plugin.launch(plugin.asyncDispatcher) {
                while (isActive) {
                    refreshed.refresh(Bukkit.getOnlinePlayers().map { it.uniqueId })
                    delay(RELATION_REFRESH_INTERVAL)
                }
            }
        }

        if (everyoneTracksEveryone) {
            log.atInfo().log("Xaero player tracker: everyone tracks everyone")
        } else if (sources.isNotEmpty()) {
            log.atInfo().log("Xaero player tracker: %d relation source(s) active", sources.size)
        }
    }

    /**
     * Starts syncing [player]'s position to others and the players they see to them. The task runs even
     * while the tracker is off, so a reload can turn it on and off for players already online.
     */
    fun track(player: Player) {
        joinedAt[player.uniqueId] = TimeSource.Monotonic.markNow()
        relations?.let { relations ->
            relations.add(player.uniqueId)
            plugin.launch(plugin.asyncDispatcher) { relations.refresh(listOf(player.uniqueId)) }
        }

        player.scheduler.runAtFixedRate(plugin, { tick(player) }, null, 1, TICK_INTERVAL)
    }

    private fun tick(player: Player) {
        // With every switch off there is nothing to read or send, once the players sent before are removed.
        val trackerOn = everyoneTracksEveryone || relations != null
        if (!trackerOn && !journeyMapRadar && !tracker.hasSent(player.uniqueId)) return

        val location = player.location
        val world = player.world.key.asString()
        tracker.update(player.uniqueId, TrackedPosition(location.x, location.y, location.z, world))

        val viewer = PaperCompanionPlayer(player)
        // Only the players related to the viewer, unless everyone tracks everyone.
        val candidates = if (everyoneTracksEveryone) null else relations?.relatedOf(player.uniqueId).orEmpty()
        tracker.tick(viewer, candidates) { target -> isVisibleOnMap(player, target) }

        if (journeyMapRadar) {
            // Read on the player's own thread; JourneyMap's own radar leaves such players out.
            val concealed = player.isSneaking || player.isInvisible || player.gameMode == GameMode.SPECTATOR
            journeyMapTracker.update(
                player.uniqueId,
                JourneyMapPlayer(player.entityId, location.x, location.y, location.z, location.yaw, location.pitch, world),
                concealed,
                // Who sees this player, read on this player's own thread for the other players' ticks.
                trackedBy = player.trackedBy.mapTo(HashSet()) { it.uniqueId },
            )

            // JourneyMap then shows only the players listed here, so the ones the client sees itself stay visible too.
            journeyMapTracker.tick(
                viewer,
                isTracked = { target -> isShownByTracker(player.uniqueId, target) && isVisibleOnMap(player, target) },
                isNearby = { target -> journeyMapTracker.sees(player.uniqueId, target) && isVisibleOnMap(player, target) },
            )
        }
    }

    private fun isShownByTracker(viewer: UUID, target: UUID) =
        everyoneTracksEveryone || relations?.isRelated(viewer, target) == true

    // The maps show whom the viewer sees in the world. Vanish hides vanished players only from the viewers whose
    // level may not see them, a little after they join, so vanished players stay off every map until then.
    private fun isVisibleOnMap(player: Player, target: UUID): Boolean {
        val targetPlayer = Bukkit.getPlayer(target) ?: return false
        val onlineFor = joinedAt[target]?.elapsedNow() ?: Duration.ZERO
        return MapVisibility.isShown(player.canSee(targetPlayer), onlineFor) { targetPlayer.isVanished }
    }

    /**
     * The tracker as it is for [player], for `/minimapcompanion info`. Read it on the player's own thread.
     */
    fun info(player: Player) = MapTrackerInfo(
        mode = when {
            everyoneTracksEveryone -> MapTrackerMode.EVERYONE
            relations != null -> MapTrackerMode.RELATIONS
            else -> MapTrackerMode.OFF
        },
        xaeroPlayers = tracker.sentCount(player.uniqueId),
    )

    /**
     * Clears the tracked players [player]'s client knows, including ones another server sent.
     */
    fun reset(player: Player) {
        tracker.reset(PaperCompanionPlayer(player))
    }

    fun forget(player: Player) {
        joinedAt.remove(player.uniqueId)
        tracker.forget(player.uniqueId)
        journeyMapTracker.forget(player.uniqueId)
        relations?.forget(player.uniqueId)
    }
}
