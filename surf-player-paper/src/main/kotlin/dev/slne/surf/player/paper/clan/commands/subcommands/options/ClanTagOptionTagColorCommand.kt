package dev.slne.surf.player.paper.clan.commands.subcommands.options

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.argument
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.player.api.clan.clan.Clan
import dev.slne.surf.player.api.clan.clan.ClanTagColor
import dev.slne.surf.player.api.clan.clan.ClanTagColorUpdate
import dev.slne.surf.player.api.clan.permission.ClanPermission
import dev.slne.surf.player.paper.clan.commands.arguments.color.ClanTagHexColorArgument
import dev.slne.surf.player.paper.clan.commands.arguments.color.ClanTagShadowHexColorArgument
import dev.slne.surf.player.paper.clan.commands.failCommand
import dev.slne.surf.player.paper.clan.permission.ClanPermissions
import dev.slne.surf.api.paper.command.executors.playerExecutorSuspend
import dev.slne.surf.player.core.client.clan.Messages
import dev.slne.surf.player.core.client.clan.command.NO_TAG_COLOR_PERMISSION
import dev.slne.surf.player.core.client.clan.command.clanTagColorChangedMessage
import net.kyori.adventure.text.format.ShadowColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Player

fun CommandAPICommand.clanTagColorCommand() = subcommand("tagcolor") {
    withPermission(ClanPermissions.CLAN_OPTIONS_TAG_COLOR_COMMAND)

    subcommand("foreground") {
        withPermission(ClanPermissions.CLAN_OPTIONS_TAG_COLOR_FOREGROUND_COMMAND)
        argument(ClanTagHexColorArgument("color"))
        playerExecutorSuspend { player, args ->
            val color: TextColor by args
            changeColor(player, ClanTagColor.update { foreground(color) })
        }
    }

    subcommand("background") {
        withPermission(ClanPermissions.CLAN_OPTIONS_TAG_COLOR_BACKGROUND_COMMAND)
        argument(ClanTagHexColorArgument("color"))
        playerExecutorSuspend { player, args ->
            val color: TextColor by args
            changeColor(player, ClanTagColor.update { background(color) })
        }
    }

    subcommand("shadow") {
        withPermission(ClanPermissions.CLAN_OPTIONS_TAG_COLOR_SHADOW_COMMAND)
        argument(ClanTagShadowHexColorArgument("color"))
        playerExecutorSuspend { player, args ->
            val color: ShadowColor by args
            changeColor(player, ClanTagColor.update { shadow(color) })
        }
    }
}

private suspend fun changeColor(sender: Player, update: ClanTagColorUpdate) {
    val clan = Clan.byPlayer(sender.uniqueId)
        ?: failCommand(Messages.NOT_IN_CLAN)

    if (!clan.hasMemberPermission(sender.uniqueId, ClanPermission.OPTIONS_TAG_COLOR)) {
        failCommand(NO_TAG_COLOR_PERMISSION)
    }

    clan.changeClanTagColor(update)

    sender.sendMessage(clanTagColorChangedMessage())
}
