package dev.slne.surf.player.paper.clan.commands.subcommands.player

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.player.paper.clan.commands.subcommands.player.subcommands.clanPlayerSettingsCommand
import dev.slne.surf.player.paper.clan.permission.ClanPermissions

fun CommandAPICommand.clanPlayerCommand() = subcommand("player") {
    withPermission(ClanPermissions.CLAN_PLAYER_COMMAND)

    clanPlayerSettingsCommand()
}