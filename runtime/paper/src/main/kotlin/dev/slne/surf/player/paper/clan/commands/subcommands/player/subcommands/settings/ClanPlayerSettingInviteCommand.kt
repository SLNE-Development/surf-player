package dev.slne.surf.player.paper.clan.commands.subcommands.player.subcommands.settings

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.booleanArgument
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.player.api.SurfCommunityPlayer
import dev.slne.surf.player.paper.clan.commands.failCommand
import dev.slne.surf.player.paper.clan.permission.ClanPermissions
import dev.slne.surf.api.paper.command.executors.playerExecutorSuspend
import dev.slne.surf.player.core.client.clan.command.NOTHING_CHANGED
import dev.slne.surf.player.core.client.clan.command.clanInvitesToggledMessage

fun CommandAPICommand.clanPlayerSettingInviteCommand() = subcommand("invite") {
    withPermission(ClanPermissions.CLAN_PLAYER_SETTINGS_INVITE_COMMAND)

    booleanArgument("accept", optional = true)

    playerExecutorSuspend { player, args ->
        val acceptByArgs = args.getUnchecked<Boolean?>("accept")
        val clanPlayer = SurfCommunityPlayer(player.uniqueId)

        val accept = acceptByArgs ?: clanPlayer.acceptsClanInvites().not()
        val changed = clanPlayer.setAcceptsClanInvites(accept)

        if (!changed) {
            failCommand(NOTHING_CHANGED)
        }

        player.sendMessage(clanInvitesToggledMessage(accept))
    }
}
