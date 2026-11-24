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
import dev.slne.surf.player.core.client.clan.command.CLAN_OWNER_CANNOT_LEAVE
import dev.slne.surf.player.core.client.clan.command.handleLeaveClick
import dev.slne.surf.player.core.client.clan.command.leaveConfirmationMessage
import dev.slne.surf.player.core.client.clan.permission.ClanPermissions
import kotlinx.coroutines.launch
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.event.ClickEvent
import net.minestom.server.entity.Player
import java.util.*
import kotlin.time.Duration.Companion.minutes

fun CommandAPICommand.clanLeaveCommand(): CommandAPICommand = withSubcommand(
    subcommand("leave") {
        withPermission(ClanPermissions.CLAN_LEAVE_COMMAND)

        playerExecutorSuspend { player, args ->
            val clan = SurfClanApi.findClanByPlayer(player.uuid)
                ?: failCommand(Messages.NOT_IN_CLAN)

            if (clan.createdByUuid == player.uuid) {
                failCommand(CLAN_OWNER_CANNOT_LEAVE)
            }

            player.sendMessage(leaveConfirmationMessage(clan, createConfirmCallback(clan.uuid)))
        }
    }
)

private fun createConfirmCallback(originalClanUuid: UUID) = ClickEvent.callback(
    ClickCallback.widen({ clicked ->
        minestomAsyncScope.launch {
            handleLeaveClick(clicked, clicked.uuid, clicked.username, originalClanUuid)
        }
    }, Player::class.java)
) { it.lifetime(1.minutes) }
