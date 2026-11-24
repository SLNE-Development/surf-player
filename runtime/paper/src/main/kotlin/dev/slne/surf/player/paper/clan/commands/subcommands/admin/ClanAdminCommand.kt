package dev.slne.surf.player.paper.clan.commands.subcommands.admin

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.player.paper.clan.commands.subcommands.admin.subcommands.clanDeleteCommand
import dev.slne.surf.player.paper.clan.commands.subcommands.admin.subcommands.clanInvalidateAllCachesCommand
import dev.slne.surf.player.paper.clan.commands.subcommands.admin.subcommands.clanReloadCommand
import dev.slne.surf.player.paper.clan.permission.ClanPermissions

fun CommandAPICommand.clanAdminCommand() = subcommand("admin") {
    withPermission(ClanPermissions.CLAN_ADMIN_COMMAND)

    clanReloadCommand()
    clanInvalidateAllCachesCommand()
    clanDeleteCommand()
}