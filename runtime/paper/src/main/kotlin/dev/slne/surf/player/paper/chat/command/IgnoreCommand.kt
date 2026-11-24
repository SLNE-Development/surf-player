package dev.slne.surf.player.paper.chat.command

import com.github.shynixn.mccoroutine.folia.scope
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.api.core.command.args.awaitingOrNull
import dev.slne.surf.player.paper.command.playerExecutorSuspend
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.player.core.client.chat.command.ChatCommands
import dev.slne.surf.player.core.client.chat.command.ignoreCommandHelp
import dev.slne.surf.player.core.client.chat.permission.ChatPermissions
import dev.slne.surf.player.core.client.command.CommandMessages
import dev.slne.surf.player.paper.command.failCommand
import dev.slne.surf.player.paper.command.helpExecutor
import dev.slne.surf.player.paper.command.page
import dev.slne.surf.player.paper.command.pageArgument
import dev.slne.surf.player.paper.command.reply
import dev.slne.surf.player.paper.command.visibleOfflinePlayerArgument
import dev.slne.surf.player.paper.plugin
import kotlinx.coroutines.future.future
import org.bukkit.entity.Player

/**
 * `/ignore add|remove|list`.
 */
fun ignoreCommand() = commandAPICommand("ignore") {
    withPermission(ChatPermissions.COMMAND_IGNORE)
    helpExecutor(ignoreCommandHelp)

    subcommand("add") {
        withPermission(ChatPermissions.COMMAND_IGNORE_ADD)
        visibleOfflinePlayerArgument("spieler")

        playerExecutorSuspend { player, args ->
            val target = args.awaitingOrNull<SurfPlayer?>("spieler") ?: failCommand(CommandMessages.PLAYER_NOT_FOUND)
            val targetName = target.lastKnownName ?: target.uuid.toString()

            player.reply(ChatCommands.ignoreAdd(player.uniqueId, target.uuid, targetName))
        }
    }

    subcommand("remove") {
        withPermission(ChatPermissions.COMMAND_IGNORE_REMOVE)
        withArguments(StringArgument("spieler").replaceSuggestions(ignoredPlayerSuggestions))

        playerExecutorSuspend { player, args ->
            val spieler: String by args
            val target = ChatCommands.findIgnored(player.uniqueId, spieler)
                ?: failCommand("Du ignorierst $spieler nicht.")

            player.reply(ChatCommands.ignoreRemove(player.uniqueId, target, spieler))
        }
    }

    subcommand("list") {
        withPermission(ChatPermissions.COMMAND_IGNORE_LIST)
        pageArgument()

        playerExecutorSuspend { player, args ->
            player.reply(ChatCommands.ignoreList(player.uniqueId, args.page))
        }
    }
}

private val ignoredPlayerSuggestions = ArgumentSuggestions.stringCollectionAsync<org.bukkit.command.CommandSender> { info ->
    plugin.scope.future {
        val player = info.sender as? Player ?: return@future emptyList()
        ChatCommands.ignoredNames(player.uniqueId)
    }
}
