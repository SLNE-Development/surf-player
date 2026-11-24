package dev.slne.surf.social.paper.clan.commands.subcommands.admin.subcommands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.social.paper.clan.permission.ClanPermissions
import dev.slne.surf.social.core.client.clan.command.clanConfigReloadedMessage
import dev.slne.surf.social.core.common.clan.config.ClanConfig

fun CommandAPICommand.clanReloadCommand() = subcommand("reload") {
    withPermission(ClanPermissions.CLAN_ADMIN_RELOAD_COMMAND)

    anyExecutor { source, args ->
        ClanConfig.reloadFromFile()

        source.sendMessage(clanConfigReloadedMessage())
    }
}
