package dev.slne.surf.player.paper.clan.commands.subcommands.player.subcommands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.player.paper.clan.commands.subcommands.player.subcommands.settings.clanPlayerSettingInviteCommand
import dev.slne.surf.player.paper.clan.permission.ClanPermissions

fun CommandAPICommand.clanPlayerSettingsCommand() = subcommand("settings") {
    withPermission(ClanPermissions.CLAN_PLAYER_SETTINGS_COMMAND)

    clanPlayerSettingInviteCommand()
}