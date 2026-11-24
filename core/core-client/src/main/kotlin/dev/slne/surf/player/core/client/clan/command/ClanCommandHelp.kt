package dev.slne.surf.player.core.client.clan.command

import dev.slne.surf.player.core.client.clan.permission.ClanPermissions
import dev.slne.surf.player.core.client.command.CommandHelp
import dev.slne.surf.player.core.client.command.HelpEntry

/**
 * The help `/clan` shows, the same on every platform.
 */
val clanCommandHelp = CommandHelp(
    "Clans",
    listOf(
        HelpEntry("/clan create <name> <tag>", "Gründet einen Clan", ClanPermissions.COMMAND_CREATE),
        HelpEntry("/clan info [clan]", "Zeigt Informationen über einen Clan", ClanPermissions.COMMAND_INFO),
        HelpEntry("/clan whois <spieler>", "Zeigt, in welchem Clan ein Spieler ist", ClanPermissions.COMMAND_WHOIS),
        HelpEntry("/clan list [seite]", "Zeigt alle Clans", ClanPermissions.COMMAND_LIST),
        HelpEntry("/clan members [clan]","Zeigt die Mitglieder eines Clans", ClanPermissions.COMMAND_MEMBERS),
        HelpEntry("/clan chat <nachricht>", "Schreibt in den Clan-Chat (kurz: /cc, /clanchat)", ClanPermissions.COMMAND_CHAT),
        HelpEntry("/clan invite <spieler>", "Lädt einen Spieler in deinen Clan ein", ClanPermissions.COMMAND_INVITE),
        HelpEntry("/clan revoke <spieler>", "Zieht eine Einladung zurück", ClanPermissions.COMMAND_REVOKE),
        HelpEntry("/clan accept <clan>", "Nimmt eine Einladung an", ClanPermissions.COMMAND_ACCEPT),
        HelpEntry("/clan deny <clan>", "Lehnt eine Einladung ab", ClanPermissions.COMMAND_DENY),
        HelpEntry("/clan promote <mitglied>", "Befördert ein Mitglied", ClanPermissions.COMMAND_PROMOTE),
        HelpEntry("/clan demote <mitglied>", "Stuft ein Mitglied herab", ClanPermissions.COMMAND_DEMOTE),
        HelpEntry("/clan kick <mitglied>", "Wirft ein Mitglied aus dem Clan", ClanPermissions.COMMAND_KICK),
        HelpEntry("/clan discord <link|remove>", "Setzt oder entfernt den Discord-Link", ClanPermissions.COMMAND_DISCORD),
        HelpEntry("/clan color <foreground|background|shadow> <farbe>", "Ändert die Farbe des Clan-Tags", ClanPermissions.COMMAND_COLOR),
        HelpEntry("/clan leave", "Verlässt deinen Clan", ClanPermissions.COMMAND_LEAVE),
        HelpEntry("/clan delete", "Löst deinen Clan auf", ClanPermissions.COMMAND_DELETE),
    )
)
