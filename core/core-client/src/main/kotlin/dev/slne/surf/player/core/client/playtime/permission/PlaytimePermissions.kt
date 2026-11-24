package dev.slne.surf.player.core.client.playtime.permission

/**
 * Every permission node of the playtime feature, the same on every platform.
 *
 * See docs/command-conventions.md for the scheme.
 */
object PlaytimePermissions {
    private const val ADMIN = "surf.player.admin.playtime"

    // Player commands (/playtime, /pt)
    const val COMMAND_PLAYTIME = "surf.player.command.playtime"

    /** Lets `/playtime <spieler>` show the playtime of other players. */
    const val OTHERS = "surf.player.playtime.others"

    // Admin actions (/surfplayer playtime ...)
    const val ADMIN_RELOAD = "$ADMIN.reload"
    const val ADMIN_RECALCULATE = "$ADMIN.recalculate"
    const val ADMIN_PAUSE = "$ADMIN.pause"
}
