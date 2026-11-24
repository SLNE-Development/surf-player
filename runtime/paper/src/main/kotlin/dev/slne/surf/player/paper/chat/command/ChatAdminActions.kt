package dev.slne.surf.player.paper.chat.command

import dev.jorel.commandapi.arguments.MapArgumentBuilder
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.optionalArgument
import dev.slne.surf.api.paper.command.executors.anyExecutorSuspend
import dev.slne.surf.player.core.client.chat.command.ChatCommands
import dev.slne.surf.player.core.client.chat.permission.ChatPermissions
import dev.slne.surf.player.paper.chat.util.realName
import dev.slne.surf.player.paper.command.PaperAdminAction
import dev.slne.surf.player.paper.command.page
import dev.slne.surf.player.paper.command.pageArgument
import dev.slne.surf.player.paper.command.reply
import dev.slne.surf.player.paper.command.toggleArgument
import org.bukkit.Bukkit

/**
 * The actions of `/surfplayer chat`.
 */
fun chatAdminActions() = listOf(
    PaperAdminAction("reload", ChatPermissions.ADMIN_RELOAD, "Lädt die Chat-Konfiguration neu") {
        anyExecutor { sender, _ -> sender.sendMessage(ChatCommands.reload()) }
    },

    PaperAdminAction(
        "lookup",
        ChatPermissions.ADMIN_LOOKUP,
        "Durchsucht den Chatverlauf (Filter: /surfplayer chat filters)",
        "[--filter wert ...]"
    ) {
        optionalArgument(
            MapArgumentBuilder<String, String>("filter", ' ')
                .withKeyMapper { it }
                .withValueMapper { it }
                .withKeyList(ChatCommands.lookupFilterKeys)
                .withoutValueList()
                .build()
        )

        anyExecutorSuspend { sender, args ->
            val filter: Map<String, String>? by args

            sender.sendMessage(ChatCommands.lookupSearching)
            sender.reply(ChatCommands.lookup(filter))
        }
    },

    PaperAdminAction("filters", ChatPermissions.ADMIN_LOOKUP, "Erklärt die Filter von lookup", "[seite]") {
        pageArgument()
        anyExecutor { sender, args -> sender.sendMessage(ChatCommands.lookupFilters(args.page)) }
    },

    PaperAdminAction("status", ChatPermissions.ADMIN_STATUS, "Zeigt, ob der Chat auf diesem Server aktiv ist") {
        anyExecutor { sender, _ -> sender.sendMessage(ChatCommands.chatStatus()) }
    },

    PaperAdminAction("toggle", ChatPermissions.ADMIN_TOGGLE, "Schaltet den Chat auf diesem Server an oder aus", "<an|aus>") {
        toggleArgument("zustand")

        anyExecutorSuspend { sender, args ->
            val zustand: Boolean by args

            sender.reply(ChatCommands.chatToggle(zustand, sender.realName()) { notification ->
                Bukkit.broadcast(notification, ChatPermissions.TEAM_NOTIFY_FUNCTIONALITY)
            })
        }
    },

    PaperAdminAction("servers", ChatPermissions.ADMIN_SERVERS, "Zeigt den Chat-Status aller Server", "[seite]") {
        pageArgument()
        anyExecutorSuspend { sender, args -> sender.sendMessage(ChatCommands.chatServers(args.page)) }
    },
)
