package dev.slne.surf.player.minestom.clan.command.subcommands.options

import dev.slne.surf.player.api.clan.SurfClanApi
import dev.slne.surf.player.api.clan.clan.Clan
import dev.slne.surf.player.api.clan.clan.ClanTagColor
import dev.slne.surf.player.api.clan.clan.ClanTagColorUpdate
import dev.slne.surf.player.api.clan.permission.ClanPermission
import dev.slne.surf.player.minestom.clan.command.arguments.color.clanTagHexColorArgument
import dev.slne.surf.player.minestom.clan.command.arguments.color.clanTagShadowHexColorArgument
import dev.slne.surf.player.minestom.command.failCommand
import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.dsl.playerExecutorSuspend
import dev.slne.surf.api.minestom.command.dsl.subcommand
import dev.slne.surf.player.core.client.clan.Messages
import dev.slne.surf.player.core.client.clan.command.NO_TAG_COLOR_PERMISSION
import dev.slne.surf.player.core.client.clan.command.clanTagColorChangedMessage
import dev.slne.surf.player.core.client.clan.permission.ClanPermissions
import net.kyori.adventure.text.format.ShadowColor
import net.kyori.adventure.text.format.TextColor
import net.minestom.server.entity.Player

fun CommandAPICommand.clanTagColorCommand(): CommandAPICommand = withSubcommand(
    subcommand("tagcolor") {
        withPermission(ClanPermissions.CLAN_OPTIONS_TAG_COLOR_COMMAND)

        withSubcommand(subcommand("foreground") {
            withPermission(ClanPermissions.CLAN_OPTIONS_TAG_COLOR_FOREGROUND_COMMAND)
            clanTagHexColorArgument("color")
            playerExecutorSuspend { player, args ->
                val color: TextColor by args
                changeColor(player, ClanTagColor.update { foreground(color) })
            }
        })

        withSubcommand(subcommand("background") {
            withPermission(ClanPermissions.CLAN_OPTIONS_TAG_COLOR_BACKGROUND_COMMAND)
            clanTagHexColorArgument("color")
            playerExecutorSuspend { player, args ->
                val color: TextColor by args
                changeColor(player, ClanTagColor.update { background(color) })
            }
        })

        withSubcommand(subcommand("shadow") {
            withPermission(ClanPermissions.CLAN_OPTIONS_TAG_COLOR_SHADOW_COMMAND)
            clanTagShadowHexColorArgument("color")
            playerExecutorSuspend { player, args ->
                val color: ShadowColor by args
                changeColor(player, ClanTagColor.update { shadow(color) })
            }
        })
    }
)

private suspend fun changeColor(sender: Player, update: ClanTagColorUpdate) {
    val clan = SurfClanApi.findClanByPlayer(sender.uuid)
        ?: failCommand(Messages.NOT_IN_CLAN)

    if (!clan.hasMemberPermission(sender.uuid, ClanPermission.OPTIONS_TAG_COLOR)) {
        failCommand(NO_TAG_COLOR_PERMISSION)
    }

    clan.changeClanTagColor(update)

    sender.sendMessage(clanTagColorChangedMessage())
}
