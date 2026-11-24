package dev.slne.surf.player.core.client.chat.command

import dev.slne.surf.player.core.client.chat.permission.ChatPermissions
import dev.slne.surf.player.core.client.command.CommandHelp
import dev.slne.surf.player.core.client.command.HelpEntry

/**
 * The help `/ignore` shows, the same on every platform.
 */
val ignoreCommandHelp = CommandHelp(
    "Ignorieren",
    listOf(
        HelpEntry("/ignore add <spieler>", "Ignoriert die Nachrichten eines Spielers", ChatPermissions.COMMAND_IGNORE_ADD),
        HelpEntry("/ignore remove <spieler>", "Ignoriert einen Spieler nicht mehr", ChatPermissions.COMMAND_IGNORE_REMOVE),
        HelpEntry("/ignore list [seite]", "Zeigt die Spieler, die du ignorierst", ChatPermissions.COMMAND_IGNORE_LIST),
    )
)

/**
 * The help `/spy` shows, the same on every platform.
 */
val spyCommandHelp = CommandHelp(
    "Spionieren",
    listOf(
        HelpEntry("/spy add <spieler>", "Liest die privaten Nachrichten eines Spielers mit"),
        HelpEntry("/spy remove <spieler>", "Liest die privaten Nachrichten eines Spielers nicht mehr mit"),
        HelpEntry("/spy clear", "Beendet das Mitlesen bei allen Spielern"),
    )
)

/**
 * The help `/slowchat` shows, the same on every platform.
 */
val slowChatCommandHelp = CommandHelp(
    "Slowchat",
    listOf(
        HelpEntry("/slowchat info", "Zeigt, ob der Slowchat aktiv ist"),
        HelpEntry("/slowchat toggle [an|aus]", "Schaltet den Slowchat an oder aus"),
    )
)
