package dev.slne.surf.social.minestom.clan.command.subcommands

import dev.slne.surf.social.api.clan.clan.Clan
import dev.slne.surf.social.api.clan.clan.ClanCreationResult
import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.dsl.playerExecutorSuspend
import dev.slne.surf.api.minestom.command.dsl.stringArgument
import dev.slne.surf.api.minestom.command.dsl.subcommand
import dev.slne.surf.social.core.common.clan.clan.ClanImpl
import dev.slne.surf.social.core.client.clan.command.clanCreatedMessage
import dev.slne.surf.social.core.client.clan.command.clanCreationFailedMessage
import dev.slne.surf.social.core.client.clan.permission.ClanPermissions

fun CommandAPICommand.clanCreateCommand(): CommandAPICommand = withSubcommand(
    subcommand("create") {
        withPermission(ClanPermissions.CLAN_CREATE_COMMAND)

        stringArgument("name")
        stringArgument("tag")

        playerExecutorSuspend { player, args ->
            val name: String by args
            val tag: String by args

            val result = Clan.createClan(name, tag, player.uuid)
            if (result !is ClanCreationResult.Success) {
                player.sendMessage(clanCreationFailedMessage(result))
                return@playerExecutorSuspend
            }

            player.sendMessage(clanCreatedMessage(result.clan as ClanImpl))
        }
    }
)
