package dev.slne.surf.player.core.client.util

import dev.slne.surf.api.core.luckperms.LuckPermsAccess
import java.util.*

/**
 * Whether the player identified by [uuid] has [permission], wherever in the network they are or
 * if they are offline: read from LuckPerms, which loads the player if this server does not have
 * them cached. For a player on this server, prefer the platform's own permission check.
 */
suspend fun hasNetworkPermission(uuid: UUID, permission: String): Boolean {
    val cachedUser = LuckPermsAccess.getUser(uuid)
    val user = cachedUser ?: LuckPermsAccess.loadUser(uuid)

    try {
        return user.cachedData.permissionData.checkPermission(permission).asBoolean()
    } finally {
        if (cachedUser == null) {
            LuckPermsAccess.luckperms.userManager.cleanupUser(user)
        }
    }
}
