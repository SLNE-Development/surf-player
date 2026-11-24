package dev.slne.surf.player.core.client.vanish

import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.cache.OfflinePlayerNameCache
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.player.core.client.vanish.permission.VanishPermissions
import java.util.*

/**
 * Whoever a command or a suggestion request comes from, independent of the platform.
 *
 * @property uuid the player's uuid, `null` for the console and other non-player senders
 * @param permissions the sender's permission check
 * @param grantedPermissions the permission nodes the sender holds with value `true`, read (once,
 * lazily) to derive their [vanishPriority]
 */
class CommandViewer(
    val uuid: UUID?,
    private val permissions: (String) -> Boolean,
    private val grantedPermissions: () -> Sequence<String> = { emptySequence() },
) {
    fun hasPermission(permission: String): Boolean = permissions(permission)

    /** The sender's vanish priority, see [VanishSight.viewerPriority]. */
    val vanishPriority: Int? by lazy { VanishSight.viewerPriority(grantedPermissions(), permissions) }

    companion object {
        /** The console and every other sender that is no player: sees everybody. */
        val CONSOLE = CommandViewer(null, { true })
    }
}

/**
 * The one place that decides which online players a command sender may learn about.
 *
 * A vanished player is hidden from every sender except (see [VanishSight.canSee])
 * - the vanished player themself,
 * - senders with [VanishPermissions.VANISH_BYPASS],
 * - senders whose vanish priority is at least the vanished player's,
 * - the console.
 *
 * Hidden players are treated as offline: they are not suggested as online players, and resolving
 * one by name as an online player fails exactly like it does for an offline player. Arguments
 * that also accept offline players still suggest and resolve a hidden player, but only among the
 * offline names, so the result looks the same as for any offline player.
 *
 * Every player argument on Paper, Minestom and Velocity goes through [network].
 */
class PlayerVisibility(private val vanishPriority: (UUID) -> Int?) {

    /** Whether [viewer] may learn that the player identified by [target] is online. */
    fun canSee(viewer: CommandViewer, target: UUID): Boolean {
        val targetPriority = vanishPriority(target) ?: return true
        val viewerUuid = viewer.uuid ?: return true

        return VanishSight.canSee(
            viewerIsTarget = viewerUuid == target,
            viewerHasBypass = viewer.hasPermission(VanishPermissions.VANISH_BYPASS),
            viewerPriority = viewer.vanishPriority,
            targetPriority = targetPriority,
        )
    }

    /** The entries of [players] that [viewer] may see; [uuidOf] identifies each entry. */
    inline fun <T> filterVisible(viewer: CommandViewer, players: Iterable<T>, uuidOf: (T) -> UUID): List<T> =
        players.filter { canSee(viewer, uuidOf(it)) }

    /** The players online in the network that [viewer] may see. */
    fun onlinePlayers(
        viewer: CommandViewer,
        online: Collection<SurfPlayer> = SurfCoreApi.getOnlinePlayers()
    ): List<SurfPlayer> = filterVisible(viewer, online) { it.uuid }

    /**
     * The names of the players online in the network that [viewer] may see and whose name starts
     * with [input] (ignoring case): the suggestions of every online-player argument.
     */
    fun onlinePlayerNames(
        viewer: CommandViewer,
        input: String = "",
        online: Collection<SurfPlayer> = SurfCoreApi.getOnlinePlayers()
    ): List<String> = online.mapNotNull { player ->
        val name = player.lastKnownName ?: return@mapNotNull null
        name.takeIf { it.startsWith(input, ignoreCase = true) && canSee(viewer, player.uuid) }
    }

    /**
     * The names suggested by every argument that accepts offline players too: the online players
     * [viewer] may see first, then known offline names. A hidden player only shows up among the
     * offline names, exactly like an offline player.
     */
    fun offlinePlayerNames(
        viewer: CommandViewer,
        input: String,
        limit: Int = SUGGESTION_LIMIT,
        online: Collection<SurfPlayer> = SurfCoreApi.getOnlinePlayers(),
        offlineNamesByPrefix: (prefix: String, limit: Int) -> List<String> = { prefix, max ->
            OfflinePlayerNameCache.findByPrefix(prefix, max)
        }
    ): List<String> {
        val suggestions = ArrayList<String>()
        val onlineNames = TreeSet(String.CASE_INSENSITIVE_ORDER)

        for (name in onlinePlayerNames(viewer, input, online)) {
            if (suggestions.size >= limit) break
            suggestions += name
            onlineNames += name
        }

        val remaining = limit - suggestions.size
        if (remaining > 0) {
            for (name in offlineNamesByPrefix(input, remaining + onlineNames.size)) {
                if (suggestions.size >= limit) break
                if (name in onlineNames) continue
                suggestions += name
            }
        }

        return suggestions
    }

    /**
     * The online player called [name] (ignoring case) if [viewer] may see them, `null` when they
     * are offline, unknown or hidden; callers answer `null` with the message they use for an
     * offline or unknown player.
     */
    fun findOnlinePlayer(
        viewer: CommandViewer,
        name: String,
        lookup: (String) -> SurfPlayer? = { SurfCoreApi.getPlayer(it) }
    ): SurfPlayer? = lookup(name)?.takeIf { canSee(viewer, it.uuid) }

    /** The online player identified by [uuid] if [viewer] may see them, `null` otherwise. */
    fun findOnlinePlayer(
        viewer: CommandViewer,
        uuid: UUID,
        lookup: (UUID) -> SurfPlayer? = { SurfCoreApi.getPlayer(it) }
    ): SurfPlayer? = lookup(uuid)?.takeIf { canSee(viewer, it.uuid) }

    companion object {
        /** How many names a player argument suggests at most. */
        const val SUGGESTION_LIMIT = 500

        /** The rules applied to the network-wide vanish state of [VanishedPlayers]. */
        val network = PlayerVisibility(VanishedPlayers::vanishPriority)
    }
}
