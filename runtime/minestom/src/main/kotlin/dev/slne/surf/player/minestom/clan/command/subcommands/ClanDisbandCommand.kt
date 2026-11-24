package dev.slne.surf.player.minestom.clan.command.subcommands

import dev.slne.surf.player.api.clan.SurfClanApi
import dev.slne.surf.player.api.clan.clan.Clan
import dev.slne.surf.player.minestom.command.failCommand
import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.dsl.playerExecutorSuspend
import dev.slne.surf.api.minestom.command.dsl.subcommand
import dev.slne.surf.api.minestom.coroutine.minestomAsyncScope
import dev.slne.surf.api.core.messages.adventure.lifetime
import dev.slne.surf.player.core.client.clan.Messages
import dev.slne.surf.player.core.client.clan.command.canBeDisbandedBy
import dev.slne.surf.player.core.client.clan.command.disbandConfirmationMessage
import dev.slne.surf.player.core.client.clan.command.handleDisbandClick
import dev.slne.surf.player.core.client.clan.permission.ClanPermissions
import kotlinx.coroutines.launch
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.event.ClickEvent
import net.minestom.server.entity.Player
import java.util.*
import kotlin.time.Duration.Companion.minutes

fun CommandAPICommand.clanDisbandCommand(): CommandAPICommand = withSubcommand(
    subcommand("disband") {
        withPermission(ClanPermissions.CLAN_DISBAND_COMMAND)

        playerExecutorSuspend { player, args ->
            val playerUuid = player.uuid
            val clan =
                SurfClanApi.findClanByPlayer(playerUuid) ?: failCommand(Messages.NOT_IN_CLAN)

            clan.canBeDisbandedBy(playerUuid)?.let { error ->
                failCommand(error.message)
            }

            player.sendMessage(disbandConfirmationMessage(clan, createDisbandClickEvent(clan.uuid)))
        }
    }
)

private fun createDisbandClickEvent(oldClanUuid: UUID): ClickEvent<*> = ClickEvent.callback(
    ClickCallback.widen({ clicked ->
        minestomAsyncScope.launch {
            handleDisbandClick(clicked, clicked.uuid, oldClanUuid)
        }
    }, Player::class.java)
) { it.lifetime(1.minutes) }
