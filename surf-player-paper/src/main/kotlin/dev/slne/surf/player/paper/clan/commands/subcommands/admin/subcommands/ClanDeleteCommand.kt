package dev.slne.surf.social.paper.clan.commands.subcommands.admin.subcommands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.argument
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.social.api.clan.clan.Clan
import dev.slne.surf.social.paper.clan.commands.arguments.ClanByClanTagArgument
import dev.slne.surf.social.paper.clan.permission.ClanPermissions
import dev.slne.surf.api.core.command.args.awaiting
import dev.slne.surf.api.paper.command.executors.anyExecutorSuspend
import dev.slne.surf.social.core.common.clan.clan.ClanImpl
import dev.slne.surf.social.core.client.clan.command.clanDeletedMessage

fun CommandAPICommand.clanDeleteCommand() = subcommand("delete") {
    withPermission(ClanPermissions.CLAN_ADMIN_DELETE_COMMAND)

    argument(ClanByClanTagArgument("clan"))

    anyExecutorSuspend { sender, arguments ->
        val clan = arguments.awaiting<Clan>("clan") as ClanImpl

        val deleted = clan.delete()
        sender.sendMessage(clanDeletedMessage(clan, deleted))
    }
}