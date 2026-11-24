package dev.slne.surf.player.paper.clan.commands.subcommands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.stringArgument
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.player.api.clan.SurfClanApi
import dev.slne.surf.player.api.clan.clan.ClanCreationResult
import dev.slne.surf.player.paper.clan.permission.ClanPermissions
import dev.slne.surf.api.paper.command.executors.playerExecutorSuspend
import dev.slne.surf.player.core.common.clan.clan.ClanImpl
import dev.slne.surf.player.core.client.clan.command.clanCreatedMessage
import dev.slne.surf.player.core.client.clan.command.clanCreationFailedMessage

fun CommandAPICommand.clanCreateCommand() = subcommand("create") {
    withPermission(ClanPermissions.CLAN_CREATE_COMMAND)

    stringArgument("name")
    stringArgument("tag")

    playerExecutorSuspend { player, args ->
        val name: String by args
        val tag: String by args

        val result = SurfClanApi.createClan(player.uniqueId, name, tag)
        if (result !is ClanCreationResult.Success) {
            player.sendMessage(clanCreationFailedMessage(result))
            return@playerExecutorSuspend
        }

        player.sendMessage(clanCreatedMessage(result.clan as ClanImpl))
    }
}
