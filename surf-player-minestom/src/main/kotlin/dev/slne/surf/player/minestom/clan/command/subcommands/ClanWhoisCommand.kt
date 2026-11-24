package dev.slne.surf.social.minestom.clan.command.subcommands

import dev.slne.surf.social.api.clan.clan.Clan
import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.dsl.playerExecutorSuspend
import dev.slne.surf.api.minestom.command.dsl.subcommand
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.social.core.client.clan.Messages
import dev.slne.surf.social.core.client.clan.command.whoisMessage
import dev.slne.surf.social.core.client.clan.permission.ClanPermissions
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.minestom.command.argument.surfOfflinePlayerArgument
import kotlinx.coroutines.Deferred

fun CommandAPICommand.clanWhoisCommand(): CommandAPICommand = withSubcommand(
    subcommand("whois") {
        withPermission(ClanPermissions.CLAN_WHOIS_COMMAND)

        surfOfflinePlayerArgument("target")

        playerExecutorSuspend { player, args ->
            val target = args.get<Deferred<SurfPlayer?>>("target").await()

            if (target == null) {
                player.sendText {
                    appendErrorPrefix()
                    error(Messages.PLAYER_NOT_FOUND)
                }
                return@playerExecutorSuspend
            }

            val targetName = target.lastKnownName ?: target.uuid.toString()
            player.sendMessage(whoisMessage(targetName, Clan.byPlayer(target.uuid)))
        }
    }
)
