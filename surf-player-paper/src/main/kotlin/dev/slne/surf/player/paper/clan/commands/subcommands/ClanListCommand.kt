package dev.slne.surf.player.paper.clan.commands.subcommands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.player.paper.clan.commands.playerClickEvent
import dev.slne.surf.player.paper.clan.permission.ClanPermissions
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.command.executors.anyExecutorSuspend
import dev.slne.surf.player.core.common.clan.clan.CoreClanService
import dev.slne.surf.player.core.client.clan.command.clanListPagination
import dev.slne.surf.player.core.client.clan.command.handleClanListClick

private val pagination = clanListPagination { clanUuid ->
    playerClickEvent { clicked -> handleClanListClick(clicked, clanUuid) }
}

fun CommandAPICommand.clanListCommand() = subcommand("list") {
    withPermission(ClanPermissions.CLAN_LIST_COMMAND)

    anyExecutorSuspend { executor, _ ->
        val clans = CoreClanService.fetchAllClansWithoutMembersSortByMemberCount()

        executor.sendText {
            appendNewline()
            append(pagination.renderComponent(clans))
        }
    }
}
