package dev.slne.surf.minimapcompanion.paper.tracker

import com.github.shynixn.mccoroutine.folia.asyncDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.api.core.util.logger
import dev.slne.surf.minimapcompanion.core.config.CompanionConfig
import dev.slne.surf.minimapcompanion.core.journeymap.JourneyMapPlayer
import dev.slne.surf.minimapcompanion.core.journeymap.JourneyMapTracker
import dev.slne.surf.minimapcompanion.core.tracker.PlayerTracker
import dev.slne.surf.minimapcompanion.core.tracker.RelationSource
import dev.slne.surf.minimapcompanion.core.tracker.TrackedPosition
import dev.slne.surf.minimapcompanion.core.tracker.TrackerRelations
import dev.slne.surf.minimapcompanion.paper.platform.PaperCompanionPlayer
import dev.slne.surf.minimapcompanion.paper.plugin
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*
import kotlin.time.Duration.Companion.seconds

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
    private var relationRefresh: Job? = null
    private var isVanished: ((Player) -> Boolean)? = null

    /**
     * Reads the tracker config and starts refreshing the relations of the enabled sources.
     * Called again on reload; players stop or start seeing each other on their next tick.
     * Sources still enabled keep their relations, so their players stay on the map meanwhile.
     */
    fun configure() {
        val config = CompanionConfig.getConfig().tracker
        everyoneTracksEveryone = config.everyoneTracksEveryone
        journeyMapRadar = config.isEnabled

        if (Bukkit.getPluginManager().isPluginEnabled("surf-vanish-paper")) {
            isVanished = VanishCheck::isVanished
        }

        val sources = buildList {
            if (config.friends) source("friends", "surf-friends-paper") { FriendsRelationSource }?.let(::add)
            if (config.clanMembers) source("clan-members", "surf-clan-paper") { ClanRelationSource }?.let(::add)
        }

        relationRefresh?.cancel()
        relationRefresh = null
        val previous = relations
        relations = null

        if (sources.isNotEmpty()) {
            val relations = TrackerRelations(sources, previous)
            for (player in Bukkit.getOnlinePlayers()) {
                relations.add(player.uniqueId)
            }
            this.relations = relations

            relationRefresh = plugin.launch(plugin.asyncDispatcher) {
                while (isActive) {
                    relations.refresh(Bukkit.getOnlinePlayers().map { it.uniqueId })
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

    // The supplier keeps the source's class, which needs the other plugin, from loading without it.
    private fun source(option: String, pluginName: String, source: () -> RelationSource): RelationSource? {
        if (Bukkit.getPluginManager().isPluginEnabled(pluginName)) return source()

        log.atWarning().log("tracker.%s is on, but %s is not enabled on this server", option, pluginName)
        return null
    }

    /**
     * Starts syncing [player]'s position to others and the players they see to them. The task runs even
     * while the tracker is off, so a reload can turn it on and off for players already online.
     */
    fun track(player: Player) {
        relations?.let { relations ->
            relations.add(player.uniqueId)
            plugin.launch(plugin.asyncDispatcher) { relations.refresh(listOf(player.uniqueId)) }
        }

        player.scheduler.runAtFixedRate(plugin, { tick(player) }, null, 1, TICK_INTERVAL)
    }

    private fun tick(player: Player) {
        val location = player.location
        val world = player.world.key.asString()
        tracker.update(player.uniqueId, TrackedPosition(location.x, location.y, location.z, world))
        journeyMapTracker.update(
            player.uniqueId,
            JourneyMapPlayer(player.entityId, location.x, location.y, location.z, location.yaw, location.pitch, world),
        )

        val viewer = PaperCompanionPlayer(player)
        tracker.tick(viewer) { viewerUuid, target -> isShownByTracker(viewerUuid, target) && isVisibleOnMap(player, target) }

        if (journeyMapRadar) {
            // JourneyMap then shows only the players listed here, so the ones the client sees itself stay visible too.
            // Players track each other within the same range, so the players tracking the viewer are the ones the
            // viewer's client sees, and they can be read on the viewer's own thread.
            val seenByClient by lazy { player.trackedBy.mapTo(HashSet()) { it.uniqueId } }
            journeyMapTracker.tick(viewer) { target ->
                isVisibleOnMap(player, target) && (isShownByTracker(player.uniqueId, target) || target in seenByClient)
            }
        }
    }

    private fun isShownByTracker(viewer: UUID, target: UUID) =
        everyoneTracksEveryone || relations?.isRelated(viewer, target) == true

    // Vanished players are never tracked: surf-vanish hides them from other players only a little
    // after they join. Players hidden from the viewer in any other way stay off the map too.
    private fun isVisibleOnMap(player: Player, target: UUID): Boolean {
        val targetPlayer = Bukkit.getPlayer(target) ?: return false
        if (isVanished?.invoke(targetPlayer) == true) return false
        return player.canSee(targetPlayer)
    }

    /**
     * Clears the tracked players [player]'s client knows, including ones another server sent.
     */
    fun reset(player: Player) {
        tracker.reset(PaperCompanionPlayer(player))
    }

    fun forget(player: Player) {
        tracker.forget(player.uniqueId)
        journeyMapTracker.forget(player.uniqueId)
        relations?.forget(player.uniqueId)
    }
}
