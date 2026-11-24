package dev.slne.surf.player.core.client.settings.command

import dev.slne.surf.player.core.client.command.CommandHelp
import dev.slne.surf.player.core.client.command.HelpEntry
import dev.slne.surf.player.core.client.settings.permission.SettingsPermissions

/**
 * The help `/settings help` shows, the same on every platform. `/settings` alone opens the menu.
 */
val settingsCommandHelp = CommandHelp(
    "Einstellungen",
    listOf(
        HelpEntry("/settings", "Öffnet das Einstellungsmenü", SettingsPermissions.COMMAND_SETTINGS),
        HelpEntry("/settings toggle <einstellung> [an|aus]", "Schaltet eine Einstellung um", SettingsPermissions.COMMAND_TOGGLE),
        HelpEntry("/settings info <einstellung>", "Zeigt den Wert einer Einstellung", SettingsPermissions.COMMAND_INFO),
    )
)
