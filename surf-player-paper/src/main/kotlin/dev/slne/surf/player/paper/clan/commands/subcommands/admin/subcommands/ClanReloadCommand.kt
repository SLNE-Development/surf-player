package dev.slne.surf.player.paper.clan.commands.subcommands.admin.subcommands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.player.paper.clan.permission.ClanPermissions
import dev.slne.surf.player.core.client.clan.command.clanConfigReloadedMessage
import dev.slne.surf.player.core.common.clan.config.ClanConfig

fun CommandAPICommand.clanReloadCommand() = subcommand("reload") {
    withPermission(ClanPermissions.CLAN_ADMIN_RELOAD_COMMAND)

    anyExecutor { source, args ->
        ClanConfig.reloadFromFile()

        source.sendMessage(clanConfigReloadedMessage())
    }
}
