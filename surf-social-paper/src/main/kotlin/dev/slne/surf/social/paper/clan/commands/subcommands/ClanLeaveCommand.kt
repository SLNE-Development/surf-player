package dev.slne.surf.social.paper.clan.commands.subcommands

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.social.api.clan.clan.Clan
import dev.slne.surf.social.paper.clan.commands.failCommand
import dev.slne.surf.social.paper.clan.permission.ClanPermissions
import dev.slne.surf.social.paper.plugin
import dev.slne.surf.api.paper.command.executors.playerExecutorSuspend
import dev.slne.surf.social.core.client.clan.Messages
import dev.slne.surf.social.core.client.clan.command.CLAN_OWNER_CANNOT_LEAVE
import dev.slne.surf.social.core.client.clan.command.handleLeaveClick
import dev.slne.surf.social.core.client.clan.command.leaveConfirmationMessage
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.entity.Player
import java.util.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

fun CommandAPICommand.clanLeaveCommand() = subcommand("leave") {
    withPermission(ClanPermissions.CLAN_LEAVE_COMMAND)

    playerExecutorSuspend { player, args ->
        val clan = Clan.byPlayer(player.uniqueId)
            ?: failCommand(Messages.NOT_IN_CLAN)

        if (clan.createdByUuid == player.uniqueId) {
            failCommand(CLAN_OWNER_CANNOT_LEAVE)
        }

        player.sendMessage(leaveConfirmationMessage(clan, createConfirmCallback(clan.uuid)))
    }
}

private fun createConfirmCallback(originalClanUuid: UUID) = ClickEvent.callback(
    ClickCallback.widen({ clicked ->
        plugin.launch {
            handleLeaveClick(clicked, clicked.uniqueId, clicked.name, originalClanUuid)
        }
    }, Player::class.java)
) { it.lifetime(1.minutes.toJavaDuration()) }
