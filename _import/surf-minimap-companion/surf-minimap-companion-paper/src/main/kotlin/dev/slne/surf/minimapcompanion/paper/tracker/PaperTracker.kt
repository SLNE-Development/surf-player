package dev.slne.surf.minimapcompanion.paper.tracker

import com.github.shynixn.mccoroutine.folia.asyncDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.api.core.util.logger
import dev.slne.surf.minimapcompanion.core.config.CompanionConfig
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

    @Volatile
    private var everyoneTracksEveryone = false

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
        tracker.update(player.uniqueId, TrackedPosition(location.x, location.y, location.z, player.world.key.asString()))
        tracker.tick(PaperCompanionPlayer(player)) { viewer, target -> isTracked(player, viewer, target) }
    }

    // Vanished players are never tracked: surf-vanish hides them from other players only a little
    // after they join. Players hidden from the viewer in any other way stay off the map too.
    private fun isTracked(player: Player, viewer: UUID, target: UUID): Boolean {
        if (!everyoneTracksEveryone && relations?.isRelated(viewer, target) != true) return false

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
        relations?.forget(player.uniqueId)
    }
}
