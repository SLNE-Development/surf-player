package dev.slne.surf.player.core.client.util

import dev.slne.surf.api.core.luckperms.LuckPermsAccess
import java.util.*

/**
 * The permission nodes the player identified by [uuid] holds with value `true`, read from the
 * LuckPerms user cached on this server; empty if LuckPerms has no such user or is not installed.
 *
 * For platforms without a way to list a player's permissions (Minestom, Velocity), e.g. to derive
 * their vanish priority.
 */
fun luckPermsGrantedPermissions(uuid: UUID): Sequence<String> {
    val permissionMap = try {
        LuckPermsAccess.getUser(uuid)?.cachedData?.permissionData?.permissionMap
    } catch (_: IllegalStateException) {
        null // LuckPerms is not loaded
    } ?: return emptySequence()

    return permissionMap.entries.asSequence().filter { it.value }.map { it.key }
}
