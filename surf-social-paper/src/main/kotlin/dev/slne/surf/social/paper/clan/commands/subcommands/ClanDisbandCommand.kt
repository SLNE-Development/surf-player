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
import dev.slne.surf.social.core.client.clan.command.canBeDisbandedBy
import dev.slne.surf.social.core.client.clan.command.disbandConfirmationMessage
import dev.slne.surf.social.core.client.clan.command.handleDisbandClick
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.entity.Player
import java.util.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

fun CommandAPICommand.clanDisbandCommand() = subcommand("disband") {
    withPermission(ClanPermissions.CLAN_DISBAND_COMMAND)

    playerExecutorSuspend { player, args ->
        val playerUuid = player.uniqueId
        val clan =
            Clan.byPlayer(playerUuid) ?: failCommand(Messages.NOT_IN_CLAN)

        clan.canBeDisbandedBy(playerUuid)?.let { error ->
            failCommand(error.message)
        }

        player.sendMessage(disbandConfirmationMessage(clan, createDisbandClickEvent(clan.uuid)))
    }
}

private fun createDisbandClickEvent(oldClanUuid: UUID): ClickEvent<*> = ClickEvent.callback(
    ClickCallback.widen({ clicked ->
        plugin.launch {
            handleDisbandClick(clicked, clicked.uniqueId, oldClanUuid)
        }
    }, Player::class.java)
) { it.lifetime(1.minutes.toJavaDuration()) }
