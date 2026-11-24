package dev.slne.surf.player.core.client.connection.permission

/**
 * Every permission node of the connection feature (Discord/Twitch account linking).
 *
 * See docs/command-conventions.md for the scheme.
 */
object ConnectionPermissions {
    private const val ADMIN = "surf.player.admin.connection"

    // Player commands (/link, /unlink)
    const val COMMAND_LINK = "surf.player.command.link"
    const val COMMAND_UNLINK = "surf.player.command.unlink"

    // Admin actions (/surfplayervelocity connection ...)
    const val ADMIN_RELOAD = "$ADMIN.reload"
    const val ADMIN_LOOKUP = "$ADMIN.lookup"
}
