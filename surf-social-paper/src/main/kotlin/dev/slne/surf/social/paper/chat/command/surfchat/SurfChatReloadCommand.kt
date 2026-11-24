package dev.slne.surf.social.paper.chat.command.surfchat

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.social.core.client.chat.config.AiModerationConfig
import dev.slne.surf.social.core.client.chat.config.chatConfigProvider
import dev.slne.surf.social.core.client.chat.util.coloredComponent
import dev.slne.surf.social.paper.chat.permission.PermissionRegistry
import kotlin.system.measureTimeMillis

fun CommandAPICommand.surfChatReloadCommand() = subcommand("reload") {
    withPermission(PermissionRegistry.COMMAND_SURFCHAT_RELOAD)
    anyExecutor { executor, _ ->
        val ms = measureTimeMillis {
            chatConfigProvider.reload()
            AiModerationConfig.reload()
        }

        executor.sendText {
            appendSuccessPrefix()
            success("Successfully reloaded plugin in ")
            append(ms.coloredComponent(25))
            success("!")
        }
    }
}