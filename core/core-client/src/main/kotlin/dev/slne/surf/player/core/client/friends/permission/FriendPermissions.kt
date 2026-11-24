package dev.slne.surf.player.core.client.friends.permission

/**
 * Every permission node of the friends feature, the same on every platform.
 *
 * See docs/command-conventions.md for the scheme.
 */
object FriendPermissions {
    private const val COMMAND = "surf.player.command.friend"

    // Player commands (/friend ...)
    const val COMMAND_FRIEND = COMMAND
    const val COMMAND_ADD = "$COMMAND.add"
    const val COMMAND_REMOVE = "$COMMAND.remove"
    const val COMMAND_LIST = "$COMMAND.list"
    const val COMMAND_INFO = "$COMMAND.info"
    const val COMMAND_REQUESTS = "$COMMAND.requests"
    const val COMMAND_ACCEPT = "$COMMAND.accept"
    const val COMMAND_DENY = "$COMMAND.deny"
    const val COMMAND_REVOKE = "$COMMAND.revoke"

    // Admin actions (/surfplayer friends ...)
    const val ADMIN_COOLDOWN = "surf.player.admin.friends.cooldown"

    // Bypasses
    /** Staff: friend requests of the holder are never on cooldown. */
    const val COOLDOWN_BYPASS = "surf.player.friends.cooldown.bypass"
}
