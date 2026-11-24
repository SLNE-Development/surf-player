package dev.slne.surf.player.core.client.settings.permission

object SettingsPermissions {
    const val BASE = "surf.settings"
    const val BASE_COMMAND = "$BASE.command"

    const val COMMAND_SETTINGS = "$BASE_COMMAND.settings"
    const val COMMAND_SURF_SETTINGS = "$BASE_COMMAND.surfsettings"
    const val COMMAND_SURF_SETTINGS_REFRESH = "$BASE_COMMAND.surfsettings.refresh"
    const val COMMAND_SURF_SETTINGS_LIST = "$BASE_COMMAND.surfsettings.list"
}
