package dev.slne.surf.player.core.client.friends.command

import dev.slne.surf.player.core.client.command.CommandHelp
import dev.slne.surf.player.core.client.command.HelpEntry
import dev.slne.surf.player.core.client.friends.permission.FriendPermissions

/**
 * The help `/friend` shows, the same on every platform.
 */
val friendCommandHelp = CommandHelp(
    "Freunde",
    listOf(
        HelpEntry("/friend add <spieler>", "Sendet eine Freundschaftsanfrage", FriendPermissions.COMMAND_ADD),
        HelpEntry("/friend list [seite]", "Zeigt deine Freunde", FriendPermissions.COMMAND_LIST),
        HelpEntry("/friend info <freund>", "Zeigt Informationen über einen Freund", FriendPermissions.COMMAND_INFO),
        HelpEntry("/friend remove <freund>", "Beendet eine Freundschaft", FriendPermissions.COMMAND_REMOVE),
        HelpEntry("/friend requests [seite]", "Zeigt deine offenen Freundschaftsanfragen", FriendPermissions.COMMAND_REQUESTS),
        HelpEntry("/friend accept <spieler>", "Nimmt eine Freundschaftsanfrage an", FriendPermissions.COMMAND_ACCEPT),
        HelpEntry("/friend deny <spieler>", "Lehnt eine Freundschaftsanfrage ab", FriendPermissions.COMMAND_DENY),
        HelpEntry("/friend revoke <spieler>", "Zieht eine gesendete Freundschaftsanfrage zurück", FriendPermissions.COMMAND_REVOKE),
    )
)
