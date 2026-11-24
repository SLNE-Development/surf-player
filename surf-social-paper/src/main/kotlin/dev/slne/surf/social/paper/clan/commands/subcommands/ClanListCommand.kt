package dev.slne.surf.social.paper.clan.commands.subcommands

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.social.paper.clan.permission.ClanPermissions
import dev.slne.surf.social.paper.plugin
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.command.executors.anyExecutorSuspend
import dev.slne.surf.social.core.common.clan.clan.CoreClanService
import dev.slne.surf.social.core.client.clan.command.clanListPagination
import dev.slne.surf.social.core.client.clan.command.handleClanListClick
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.entity.Player


private val pagination = clanListPagination { clanUuid ->
    ClickEvent.callback(ClickCallback.widen({ clicked ->
        plugin.launch {
            handleClanListClick(clicked, clanUuid)
        }
    }, Player::class.java))
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
