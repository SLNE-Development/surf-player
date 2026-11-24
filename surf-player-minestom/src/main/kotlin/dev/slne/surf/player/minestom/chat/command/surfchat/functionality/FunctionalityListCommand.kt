package dev.slne.surf.social.minestom.chat.command.surfchat.functionality

import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.dsl.anyExecutorSuspend
import dev.slne.surf.api.minestom.command.dsl.integerArgument
import dev.slne.surf.api.minestom.command.dsl.subcommand
import dev.slne.surf.api.core.messages.CommonComponents
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.core.messages.pagination.Pagination
import dev.slne.surf.social.core.client.chat.message.format.appendStatusIcon
import dev.slne.surf.social.core.client.chat.permission.ChatPermissions
import dev.slne.surf.social.core.common.chat.service.FunctionalityService
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

fun CommandAPICommand.functionalityListCommand(): CommandAPICommand = withSubcommand(
    subcommand("list") {
        withPermission(ChatPermissions.COMMAND_SURFCHAT_FUNCTIONALITY_LIST)
        integerArgument("page", 1, Int.MAX_VALUE, true)
        anyExecutorSuspend { sender, args ->
            val page = args.getOptional<Int>("page") ?: 1

            sender.sendText {
                appendInfoPrefix()
                info("Lädt...")
            }

            val content = FunctionalityService.getFunctionalitiesForAllServers()
                .map { (server, functionalities) ->
                    FunctionalityStatusEntry(server, functionalities.localChatEnabled)
                }

            sender.sendText {
                append(pagination.renderComponent(content, page))
            }
        }
    }
)

private data class FunctionalityStatusEntry(
    val name: String,
    val status: Boolean
)
