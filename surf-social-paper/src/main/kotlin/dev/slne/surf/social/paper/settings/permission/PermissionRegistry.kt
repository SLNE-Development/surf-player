package dev.slne.surf.social.paper.settings.permission

import dev.slne.surf.api.paper.permission.PermissionRegistry
import dev.slne.surf.social.core.client.settings.permission.SettingsPermissions

object PermissionRegistry : PermissionRegistry() {
    val COMMAND_SETTINGS = create(SettingsPermissions.COMMAND_SETTINGS)
    val COMMAND_SURF_SETTINGS = create(SettingsPermissions.COMMAND_SURF_SETTINGS)
    val COMMAND_SURF_SETTINGS_REFRESH =
        create(SettingsPermissions.COMMAND_SURF_SETTINGS_REFRESH)
    val COMMAND_SURF_SETTINGS_LIST = create(SettingsPermissions.COMMAND_SURF_SETTINGS_LIST)
}
