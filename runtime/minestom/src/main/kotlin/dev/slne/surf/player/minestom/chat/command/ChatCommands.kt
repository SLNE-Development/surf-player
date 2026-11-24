package dev.slne.surf.player.minestom.chat.command

import dev.slne.surf.api.minestom.command.argument.StringArgument
import dev.slne.surf.api.minestom.command.dsl.anyExecutor
import dev.slne.surf.api.minestom.command.dsl.anyExecutorSuspend
import dev.slne.surf.api.minestom.command.dsl.commandAPICommand
import dev.slne.surf.api.minestom.command.dsl.greedyStringArgument
import dev.slne.surf.api.minestom.command.dsl.playerArgument
import dev.slne.surf.api.minestom.command.dsl.playerExecutor
import dev.slne.surf.api.minestom.command.dsl.playerExecutorSuspend
import dev.slne.surf.api.minestom.command.dsl.signedMessageArgument
import dev.slne.surf.api.minestom.command.dsl.subcommand
import dev.slne.surf.api.minestom.command.suggestion.ArgumentSuggestions
import dev.slne.surf.api.minestom.permission.hasPermission
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.minestom.command.argument.surfOfflinePlayerArgument
import dev.slne.surf.core.api.minestom.command.argument.surfPlayerArgument
import dev.slne.surf.player.api.chat.message.MessageData
import dev.slne.surf.player.api.chat.message.MessageType
import dev.slne.surf.player.core.client.SurfPlayerClient.redisApi
import dev.slne.surf.player.core.client.chat.command.ChatCommands
import dev.slne.surf.player.core.client.chat.command.ignoreCommandHelp
import dev.slne.surf.player.core.client.chat.command.slowChatCommandHelp
import dev.slne.surf.player.core.client.chat.command.spyCommandHelp
import dev.slne.surf.player.core.client.chat.message.format.formatTeamchat
import dev.slne.surf.player.core.client.chat.permission.ChatPermissions
import dev.slne.surf.player.core.client.chat.platform.ChatPlatform
import dev.slne.surf.player.core.client.chat.redis.event.TeamBroadcastRedisEvent
import dev.slne.surf.player.core.client.chat.service.HistoryService
import dev.slne.surf.player.core.client.chat.service.ReplyCache
import dev.slne.surf.player.core.client.command.CommandMessages
import dev.slne.surf.player.minestom.chat.service.DirectMessageService
import dev.slne.surf.player.minestom.command.MinestomAdminAction
import dev.slne.surf.player.minestom.command.failCommand
import dev.slne.surf.player.minestom.command.helpExecutor
import dev.slne.surf.player.minestom.command.page
import dev.slne.surf.player.minestom.command.pageArgument
import dev.slne.surf.player.minestom.command.reply
import dev.slne.surf.player.minestom.command.toggleArgument
import kotlinx.coroutines.Deferred
import net.kyori.adventure.chat.SignedMessage
import net.kyori.adventure.text.Component
import net.minestom.server.command.CommandSender
import net.minestom.server.entity.Player
import java.time.OffsetDateTime
import java.util.*

private const val PLAYER = "spieler"

private val CommandSender.displayedName: String
    get() = (this as? Player)?.username ?: "Konsole"

/**
 * Registers every player and team command of the chat feature.
 */
fun registerChatCommands() {
    directMessageCommand()
    replyCommand()
    ignoreCommand()
    teamchatCommand()
    spyCommand()
    slowChatCommand()
}

/** `/msg <spieler> <nachricht>` with the aliases `/dm`, `/w`, `/whisper`, `/tell` and `/pm`. */
private fun directMessageCommand() = commandAPICommand("msg") {
    withPermission(ChatPermissions.COMMAND_MSG)
    withAliases("dm", "w", "whisper", "tell", "pm")
    surfPlayerArgument(PLAYER)
    signedMessageArgument("nachricht")

    playerExecutorSuspend { player, args ->
        val spieler: SurfPlayer by args
        val nachricht: SignedMessage by args

        if (spieler.uuid == player.uuid) failCommand(ChatCommands.CANNOT_MESSAGE_YOURSELF)

        DirectMessageService.sendMessage(player, nachricht, spieler.uuid)
    }
}

/** `/reply <nachricht>` (`/r`): answers the last private message. */
private fun replyCommand() = commandAPICommand("reply") {
    withPermission(ChatPermissions.COMMAND_REPLY)
    withAliases("r")
    signedMessageArgument("nachricht")

    playerExecutorSuspend { player, args ->
        val nachricht: SignedMessage by args
        val target = ReplyCache.getLastTarget(player.uuid) ?: failCommand(ChatCommands.NO_REPLY_TARGET)

        if (target == player.uuid) failCommand(ChatCommands.CANNOT_MESSAGE_YOURSELF)
        if (SurfCoreApi.getPlayer(target) == null) failCommand(CommandMessages.PLAYER_NOT_ONLINE)

        DirectMessageService.sendMessage(player, nachricht, target)
    }
}

/** `/ignore add|remove|list`. */
private fun ignoreCommand() = commandAPICommand("ignore") {
    withPermission(ChatPermissions.COMMAND_IGNORE)
    helpExecutor(ignoreCommandHelp)

    withSubcommand(subcommand("add") {
        withPermission(ChatPermissions.COMMAND_IGNORE_ADD)
        surfOfflinePlayerArgument(PLAYER)

        playerExecutorSuspend { player, args ->
            val target = args.get<Deferred<SurfPlayer?>>(PLAYER).await()
                ?: failCommand(CommandMessages.PLAYER_NOT_FOUND)

            player.reply(ChatCommands.ignoreAdd(player.uuid, target.uuid, target.lastKnownName ?: "${target.uuid}"))
        }
    })

    withSubcommand(subcommand("remove") {
        withPermission(ChatPermissions.COMMAND_IGNORE_REMOVE)
        withArguments(StringArgument(PLAYER).replaceSuggestions(ArgumentSuggestions.stringCollectionAsync { info ->
            val player = info.sender as? Player ?: return@stringCollectionAsync emptyList()
            ChatCommands.ignoredNames(player.uuid)
        }))

        playerExecutorSuspend { player, args ->
            val spieler: String by args
            val target = ChatCommands.findIgnored(player.uuid, spieler) ?: failCommand("Du ignorierst $spieler nicht.")

            player.reply(ChatCommands.ignoreRemove(player.uuid, target, spieler))
        }
    })

    withSubcommand(subcommand("list") {
        withPermission(ChatPermissions.COMMAND_IGNORE_LIST)
        pageArgument()

        playerExecutorSuspend { player, args ->
            player.reply(ChatCommands.ignoreList(player.uuid, args.page))
        }
    })
}

/** `/teamchat <nachricht>` (`/tc`): a message to every team member in the network. */
private fun teamchatCommand() = commandAPICommand("teamchat") {
    withAliases("tc")
    withPermission(ChatPermissions.TEAM_TEAMCHAT)
    greedyStringArgument("nachricht")

    playerExecutorSuspend { player, args ->
        val nachricht: String by args
        val messageData = MessageData(
            Component.text(nachricht),
            UUID.randomUUID(),
            player.uuid,
            null,
            OffsetDateTime.now(),
            SurfCoreApi.getCurrentServerName(),
            null,
            MessageType.TEAM
        )

        redisApi.publishEvent(
            TeamBroadcastRedisEvent(formatTeamchat(messageData), ChatPermissions.TEAM_TEAMCHAT)
        ).await()

        HistoryService.logMessage(messageData)
    }
}

/** `/spy add|remove|clear`: reading the private messages of players on this server. */
private fun spyCommand() = commandAPICommand("spy") {
    withPermission(ChatPermissions.TEAM_SPY)
    helpExecutor(spyCommandHelp)

    withSubcommand(subcommand("add") {
        playerArgument(PLAYER)
        playerExecutor { player, args ->
            val spieler: Player by args
            val isTeam = spieler.hasPermission(ChatPermissions.BYPASS_SPY)

            player.reply(ChatCommands.spyAdd(player.uuid, spieler.uuid, spieler.username, isTeam))
        }
    })

    withSubcommand(subcommand("remove") {
        playerArgument(PLAYER)
        playerExecutor { player, args ->
            val spieler: Player by args
            player.reply(ChatCommands.spyRemove(player.uuid, spieler.uuid, spieler.username))
        }
    })

    withSubcommand(subcommand("clear") {
        playerExecutor { player, _ -> player.reply(ChatCommands.spyClear(player.uuid)) }
    })
}

/** `/slowchat info|toggle [an|aus]`. */
private fun slowChatCommand() = commandAPICommand("slowchat") {
    withPermission(ChatPermissions.TEAM_SLOWCHAT)
    helpExecutor(slowChatCommandHelp)

    withSubcommand(subcommand("info") {
        anyExecutor { sender, _ -> sender.sendMessage(ChatCommands.slowChatInfo()) }
    })

    withSubcommand(subcommand("toggle") {
        toggleArgument("zustand", optional = true)

        anyExecutor { sender, args ->
            val zustand = args.getOptional<Boolean>("zustand")

            sender.reply(ChatCommands.slowChatToggle(zustand, sender.displayedName) { notification ->
                ChatPlatform.broadcast(notification, ChatPermissions.SLOW_CHAT_NOTIFY)
            })
        }
    })
}

/**
 * The actions of `/surfplayer chat`.
 */
fun chatAdminActions() = listOf(
    MinestomAdminAction("reload", ChatPermissions.ADMIN_RELOAD, "Lädt die Chat-Konfiguration neu") {
        anyExecutor { sender, _ -> sender.sendMessage(ChatCommands.reload()) }
    },

    MinestomAdminAction(
        "lookup",
        ChatPermissions.ADMIN_LOOKUP,
        "Durchsucht den Chatverlauf (Filter: /surfplayer chat filters)",
        "[--filter wert ...]"
    ) {
        greedyStringArgument("filter", optional = true)

        anyExecutorSuspend { sender, args ->
            val filter = args.getOptional<String>("filter")

            sender.sendMessage(ChatCommands.lookupSearching)
            sender.reply(ChatCommands.lookup(filter?.let(ChatCommands::parseLookupQuery)))
        }
    },

    MinestomAdminAction("filters", ChatPermissions.ADMIN_LOOKUP, "Erklärt die Filter von lookup", "[seite]") {
        pageArgument()
        anyExecutor { sender, args -> sender.sendMessage(ChatCommands.lookupFilters(args.page)) }
    },

    MinestomAdminAction("status", ChatPermissions.ADMIN_STATUS, "Zeigt, ob der Chat auf diesem Server aktiv ist") {
        anyExecutor { sender, _ -> sender.sendMessage(ChatCommands.chatStatus()) }
    },

    MinestomAdminAction("toggle", ChatPermissions.ADMIN_TOGGLE, "Schaltet den Chat auf diesem Server an oder aus", "<an|aus>") {
        toggleArgument("zustand")

        anyExecutorSuspend { sender, args ->
            val zustand: Boolean by args

            sender.reply(ChatCommands.chatToggle(zustand, sender.displayedName) { notification ->
                ChatPlatform.broadcast(notification, ChatPermissions.TEAM_NOTIFY_FUNCTIONALITY)
            })
        }
    },

    MinestomAdminAction("servers", ChatPermissions.ADMIN_SERVERS, "Zeigt den Chat-Status aller Server", "[seite]") {
        pageArgument()
        anyExecutorSuspend { sender, args -> sender.sendMessage(ChatCommands.chatServers(args.page)) }
    },
)
