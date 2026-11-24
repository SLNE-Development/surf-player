package dev.slne.surf.player.paper.chat.command

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.entitySelectorArgumentOnePlayer
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.greedyStringArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.api.paper.command.executors.playerExecutorSuspend
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.player.api.chat.message.MessageData
import dev.slne.surf.player.api.chat.message.MessageType
import dev.slne.surf.player.core.client.SurfPlayerClient
import dev.slne.surf.player.core.client.chat.command.ChatCommands
import dev.slne.surf.player.core.client.chat.command.slowChatCommandHelp
import dev.slne.surf.player.core.client.chat.command.spyCommandHelp
import dev.slne.surf.player.core.client.chat.message.format.formatTeamchat
import dev.slne.surf.player.core.client.chat.permission.ChatPermissions
import dev.slne.surf.player.core.client.chat.redis.event.TeamBroadcastRedisEvent
import dev.slne.surf.player.core.client.chat.service.HistoryService
import dev.slne.surf.player.paper.chat.util.realName
import dev.slne.surf.player.paper.command.helpExecutor
import dev.slne.surf.player.paper.command.reply
import dev.slne.surf.player.paper.command.toggleArgument
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.time.OffsetDateTime
import java.util.*

/**
 * `/teamchat <nachricht>` (`/tc`): a message to every team member in the network.
 */
fun teamchatCommand() = commandAPICommand("teamchat") {
    withAliases("tc")
    withPermission(ChatPermissions.TEAM_TEAMCHAT)
    greedyStringArgument("nachricht")

    playerExecutorSuspend { player, args ->
        val nachricht: String by args
        val messageData = MessageData(
            Component.text(nachricht),
            UUID.randomUUID(),
            player.uniqueId,
            null,
            OffsetDateTime.now(),
            SurfCoreApi.getCurrentServerName(),
            null,
            MessageType.TEAM
        )

        SurfPlayerClient.redisApi.publishEvent(
            TeamBroadcastRedisEvent(formatTeamchat(messageData), ChatPermissions.TEAM_TEAMCHAT)
        ).await()

        HistoryService.logMessage(messageData)
    }
}

/**
 * `/spy add|remove|clear`: reading the private messages of players on this server.
 */
fun spyCommand() = commandAPICommand("spy") {
    withPermission(ChatPermissions.TEAM_SPY)
    helpExecutor(spyCommandHelp)

    subcommand("add") {
        entitySelectorArgumentOnePlayer("spieler")
        playerExecutor { player, args ->
            val spieler: Player by args
            val isTeam = spieler.hasPermission(ChatPermissions.BYPASS_SPY)

            player.reply(ChatCommands.spyAdd(player.uniqueId, spieler.uniqueId, spieler.name, isTeam))
        }
    }

    subcommand("remove") {
        entitySelectorArgumentOnePlayer("spieler")
        playerExecutor { player, args ->
            val spieler: Player by args

            player.reply(ChatCommands.spyRemove(player.uniqueId, spieler.uniqueId, spieler.name))
        }
    }

    subcommand("clear") {
        playerExecutor { player, _ ->
            player.reply(ChatCommands.spyClear(player.uniqueId))
        }
    }
}

/**
 * `/slowchat info|toggle [an|aus]`.
 */
fun slowChatCommand() = commandAPICommand("slowchat") {
    withPermission(ChatPermissions.TEAM_SLOWCHAT)
    helpExecutor(slowChatCommandHelp)

    subcommand("info") {
        anyExecutor { sender, _ -> sender.sendMessage(ChatCommands.slowChatInfo()) }
    }

    subcommand("toggle") {
        toggleArgument("zustand", optional = true)

        anyExecutor { sender, args ->
            val zustand: Boolean? by args

            sender.reply(ChatCommands.slowChatToggle(zustand, sender.realName()) { notification ->
                Bukkit.broadcast(notification, ChatPermissions.SLOW_CHAT_NOTIFY)
            })
        }
    }
}
