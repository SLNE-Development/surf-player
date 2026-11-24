package dev.slne.surf.social.paper.chat.command.slowchat

import dev.jorel.commandapi.kotlindsl.*
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.social.paper.chat.command.argument.NiceToggleArgument
import dev.slne.surf.social.paper.chat.permission.PermissionRegistry
import dev.slne.surf.social.core.client.chat.service.SlowChatService
import dev.slne.surf.social.paper.chat.util.realName
import org.bukkit.Bukkit

fun slowChatCommand() = commandTree("slowchat") {
    withPermission(PermissionRegistry.SLOW_CHAT_COMMAND)
    literalArgument("#status") {
        anyExecutor { sender, _ ->
            val status = SlowChatService.isSlowChat()

            sender.sendText {
                appendInfoPrefix()
                info("Der Slowchat ist derzeit ")
                variableValue(if (status) "aktiviert" else "deaktiviert")
                info(".")
            }
        }
    }

    argument(NiceToggleArgument("change")) {
        anyExecutor { sender, arguments ->
            val change: Boolean by arguments

            if (change == SlowChatService.isSlowChat()) {
                sender.sendText {
                    appendErrorPrefix()
                    error("Der Slowchat ist bereits ")
                    error(if (change) "aktiviert" else "deaktiviert")
                    error(".")
                }
                return@anyExecutor
            }

            SlowChatService.setSlowChat(change)

            Bukkit.broadcast(buildText {
                appendInfoPrefix()
                info("Der Slowchat wurde von ")
                variableValue(sender.realName())
                appendSpace()
                variableValue(if (change) "aktiviert" else "deaktiviert")
                info(".")
            }, PermissionRegistry.SLOW_CHAT_NOTIFY)

            sender.sendText {
                appendSuccessPrefix()
                success("Der Slowchat wurde erfolgreich ")
                variableValue(if (change) "aktiviert" else "deaktiviert")
                success(".")
            }
        }
    }
}