package dev.slne.surf.player.core.client.playtime.permission

/**
 * Platform-neutral registry of all permission node strings used by surf-playtime.
 */
object PlaytimePermissions {
    private const val PREFIX = "surf.playtime"

    const val COMMAND = "$PREFIX.command"
    const val COMMAND_OTHERS = "$COMMAND.others"
    const val COMMAND_ADMIN = "$COMMAND.admin"
}
