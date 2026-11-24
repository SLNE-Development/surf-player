package dev.slne.surf.social.paper.clan.commands.subcommands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.stringArgument
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.social.api.clan.clan.Clan
import dev.slne.surf.social.api.clan.clan.ClanCreationResult
import dev.slne.surf.social.paper.clan.permission.ClanPermissions
import dev.slne.surf.api.paper.command.executors.playerExecutorSuspend
import dev.slne.surf.social.core.common.clan.clan.ClanImpl
import dev.slne.surf.social.core.client.clan.command.clanCreatedMessage
import dev.slne.surf.social.core.client.clan.command.clanCreationFailedMessage

fun CommandAPICommand.clanCreateCommand() = subcommand("create") {
    withPermission(ClanPermissions.CLAN_CREATE_COMMAND)

    stringArgument("name")
    stringArgument("tag")

    playerExecutorSuspend { player, args ->
        val name: String by args
        val tag: String by args

        val result = Clan.createClan(name, tag, player.uniqueId)
        if (result !is ClanCreationResult.Success) {
            player.sendMessage(clanCreationFailedMessage(result))
            return@playerExecutorSuspend
        }

        player.sendMessage(clanCreatedMessage(result.clan as ClanImpl))
    }
}
