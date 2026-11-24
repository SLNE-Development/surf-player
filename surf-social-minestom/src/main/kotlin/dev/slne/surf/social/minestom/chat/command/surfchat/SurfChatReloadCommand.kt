package dev.slne.surf.social.minestom.chat.command.surfchat

import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.dsl.anyExecutor
import dev.slne.surf.api.minestom.command.dsl.subcommand
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.social.core.client.chat.config.AiModerationConfig
import dev.slne.surf.social.core.client.chat.config.chatConfigProvider
import dev.slne.surf.social.core.client.chat.permission.ChatPermissions
import dev.slne.surf.social.core.client.chat.util.coloredComponent
import kotlin.system.measureTimeMillis

fun CommandAPICommand.surfChatReloadCommand(): CommandAPICommand = withSubcommand(
    subcommand("reload") {
        withPermission(ChatPermissions.COMMAND_SURFCHAT_RELOAD)
        anyExecutor { executor, _ ->
            val ms = measureTimeMillis {
                chatConfigProvider.reloadFromFile()
                AiModerationConfig.reload()
            }

            executor.sendText {
                appendSuccessPrefix()
                success("Successfully reloaded chat configuration in ")
                append(ms.coloredComponent(25))
                success("!")
            }
        }
    }
)
