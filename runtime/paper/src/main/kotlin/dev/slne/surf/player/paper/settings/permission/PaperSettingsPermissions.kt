package dev.slne.surf.player.paper.settings.permission

import dev.slne.surf.api.paper.permission.PermissionRegistry
import dev.slne.surf.player.core.client.settings.permission.SettingsPermissions

object PaperSettingsPermissions : PermissionRegistry() {
    val COMMAND_SETTINGS = create(SettingsPermissions.COMMAND_SETTINGS)
    val COMMAND_SURF_SETTINGS = create(SettingsPermissions.COMMAND_SURF_SETTINGS)
    val COMMAND_SURF_SETTINGS_REFRESH =
        create(SettingsPermissions.COMMAND_SURF_SETTINGS_REFRESH)
    val COMMAND_SURF_SETTINGS_LIST = create(SettingsPermissions.COMMAND_SURF_SETTINGS_LIST)
}
