package dev.slne.surf.player.minestom.clan.command.subcommands

import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.dsl.anyExecutorSuspend
import dev.slne.surf.api.minestom.command.dsl.subcommand
import dev.slne.surf.api.minestom.coroutine.minestomAsyncScope
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.player.core.common.clan.clan.CoreClanService
import dev.slne.surf.player.core.client.clan.command.clanListPagination
import dev.slne.surf.player.core.client.clan.command.handleClanListClick
import dev.slne.surf.player.core.client.clan.permission.ClanPermissions
import kotlinx.coroutines.launch
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.event.ClickEvent
import net.minestom.server.entity.Player

private val pagination = clanListPagination { clanUuid ->
    ClickEvent.callback(ClickCallback.widen({ clicked ->
        minestomAsyncScope.launch {
            handleClanListClick(clicked, clanUuid)
        }
    }, Player::class.java))
}

fun CommandAPICommand.clanListCommand(): CommandAPICommand = withSubcommand(
    subcommand("list") {
        withPermission(ClanPermissions.CLAN_LIST_COMMAND)

        anyExecutorSuspend { executor, _ ->
            val clans = CoreClanService.fetchAllClansWithoutMembersSortByMemberCount()

            executor.sendText {
                appendNewline()
                append(pagination.renderComponent(clans))
            }
        }
    }
)
