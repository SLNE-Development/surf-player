package dev.slne.surf.player.paper.vanish.util

import dev.slne.surf.player.paper.plugin
import dev.slne.surf.player.core.client.vanish.permission.VanishPermissions
import dev.slne.surf.player.paper.vanish.service.VanishService
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue

/** Whether this player is vanished anywhere in the network. */
val OfflinePlayer.isVanished: Boolean get() = VanishService.isVanished(uniqueId)

val Player.currentTarget get() = VanishService.current(this)

fun Player.getVanishPriority(): Int {
    return effectivePermissions
        .asSequence()
        .filter { it.value }
        .map { it.permission }
        .filter { it.startsWith(VanishPermissions.VANISH_PRIORITY) }
        .mapNotNull {
            it.removePrefix("${VanishPermissions.VANISH_PRIORITY}.")
                .toIntOrNull()
        }
        .maxOrNull() ?: 0
}

fun Player.canVanishSee(target: Player): Boolean {
    if (this.uniqueId == target.uniqueId) return true

    if (this.hasPermission(VanishPermissions.VANISH_BYPASS)) return true

    val ownPriority = this.getVanishPriority()
    val targetPriority = target.getVanishPriority()

    return ownPriority >= targetPriority
}

@Suppress("DEPRECATION")
fun Player.setMetaVanished(vanished: Boolean) {
    if (vanished) {
        this.setMetadata("vanished", FixedMetadataValue(plugin, true))
    } else {
        this.removeMetadata("vanished", plugin)
    }
}
