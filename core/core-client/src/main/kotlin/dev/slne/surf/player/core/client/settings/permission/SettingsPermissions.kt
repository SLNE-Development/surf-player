package dev.slne.surf.player.core.client.settings.permission

/**
 * Every permission node of the settings feature, the same on every platform.
 *
 * See docs/command-conventions.md for the scheme.
 */
object SettingsPermissions {
    private const val COMMAND = "surf.player.command.settings"
    private const val ADMIN = "surf.player.admin.settings"

    // Player commands (/settings ...)
    const val COMMAND_SETTINGS = COMMAND
    const val COMMAND_TOGGLE = "$COMMAND.toggle"
    const val COMMAND_INFO = "$COMMAND.info"

    // Admin actions (/surfplayer settings ...)
    const val ADMIN_REFRESH = "$ADMIN.refresh"
    const val ADMIN_LIST = "$ADMIN.list"
}
