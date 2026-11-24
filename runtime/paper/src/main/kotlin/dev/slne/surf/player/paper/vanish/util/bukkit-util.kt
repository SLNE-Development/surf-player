package dev.slne.surf.player.paper.vanish.util

import dev.slne.surf.player.paper.plugin
import dev.slne.surf.player.core.client.vanish.VanishSight
import dev.slne.surf.player.core.client.vanish.VanishedPlayers
import dev.slne.surf.player.core.client.vanish.permission.VanishPermissions
import dev.slne.surf.player.paper.vanish.service.VanishService
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue

/** Whether this player is vanished anywhere in the network. */
val OfflinePlayer.isVanished: Boolean get() = VanishService.isVanished(uniqueId)

val Player.currentTarget get() = VanishService.current(this)

/** The permission nodes this player holds with value `true`. */
fun Player.grantedPermissions(): Sequence<String> =
    effectivePermissions.asSequence().filter { it.value }.map { it.permission }

/** This player's vanish priority as a viewer, see [VanishSight.viewerPriority]. */
fun Player.viewerVanishPriority(): Int? = VanishSight.viewerPriority(grantedPermissions(), ::hasPermission)

/** The priority this player is hidden with when vanishing, see [VanishSight.targetPriority]. */
fun Player.targetVanishPriority(): Int = VanishSight.targetPriority(grantedPermissions())

/**
 * Whether this player sees the vanished [target], see [VanishSight.canSee]. The target's priority
 * is the one stored network-wide in [VanishedPlayers] while they are vanished.
 */
fun Player.canVanishSee(target: Player): Boolean = VanishSight.canSee(
    viewerIsTarget = uniqueId == target.uniqueId,
    viewerHasBypass = hasPermission(VanishPermissions.VANISH_BYPASS),
    viewerPriority = viewerVanishPriority(),
    targetPriority = VanishedPlayers.vanishPriority(target.uniqueId) ?: target.targetVanishPriority(),
)

@Suppress("DEPRECATION")
fun Player.setMetaVanished(vanished: Boolean) {
    if (vanished) {
        this.setMetadata("vanished", FixedMetadataValue(plugin, true))
    } else {
        this.removeMetadata("vanished", plugin)
    }
}
