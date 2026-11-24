package dev.slne.surf.social.paper.chat.command.surfchat.functionality

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.integerArgument
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.api.core.messages.CommonComponents
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.core.messages.pagination.Pagination
import dev.slne.surf.api.paper.command.executors.anyExecutorSuspend
import dev.slne.surf.social.core.common.chat.service.FunctionalityService
import dev.slne.surf.social.paper.chat.permission.PermissionRegistry
import dev.slne.surf.social.core.client.chat.message.format.appendStatusIcon
import net.kyori.adventure.text.format.TextDecoration

private val pagination = Pagination<FunctionalityStatusEntry> {
    title { primary("Chat Funktionalität", TextDecoration.BOLD) }
    rowRenderer { row, _ ->
        listOf(
            buildText {
                append(CommonComponents.EM_DASH)
                appendSpace()
                variableKey(row.name)
                spacer(":")
                appendSpace()
                appendStatusIcon(row.status)
            }
        )
    }
}

fun CommandAPICommand.functionalityListCommand() = subcommand("list") {
    withPermission(PermissionRegistry.COMMAND_SURFCHAT_FUNCTIONALITY_LIST)
    integerArgument("page", 1, Int.MAX_VALUE, true)
    anyExecutorSuspend { player, args ->
        val page = args.getOrDefaultUnchecked("page", 1)

        player.sendText {
            appendInfoPrefix()
            info("Lädt...")
        }

        val content = FunctionalityService.getFunctionalitiesForAllServers().map { (server, functionalities) ->
            FunctionalityStatusEntry(
                server,
                functionalities.localChatEnabled
            )
        }

        player.sendText {
            append(pagination.renderComponent(content, page))
        }
    }
}

private data class FunctionalityStatusEntry(
    val name: String,
    val status: Boolean
)