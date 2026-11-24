package dev.slne.surf.player.paper.command

import com.github.shynixn.mccoroutine.folia.scope
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.CustomArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.player.core.client.command.CommandMessages
import dev.slne.surf.player.core.client.vanish.CommandViewer
import dev.slne.surf.player.core.client.vanish.PlayerVisibility
import dev.slne.surf.player.paper.plugin
import dev.slne.surf.player.paper.vanish.util.grantedPermissions
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.asDeferred
import kotlinx.coroutines.future.future
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/*
 * Every player argument of the Paper commands. Suggestions and resolution go through
 * PlayerVisibility, so a vanished player is neither suggested to nor resolved for a sender that may
 * not see them; such a sender gets the same answer as for an offline or unknown player.
 */

private val visibility get() = PlayerVisibility.network

/** This sender as seen by [PlayerVisibility]: a player, or the console for everything else. */
fun CommandSender.asViewer(): CommandViewer =
    if (this is Player) CommandViewer(uniqueId, ::hasPermission) { grantedPermissions() } else CommandViewer.CONSOLE

/**
 * A player online anywhere in the network, resolved to their [SurfPlayer]. Suggests and accepts
 * only players the sender may see; anybody else fails with [CommandMessages.PLAYER_NOT_FOUND].
 */
class VisiblePlayerArgument(nodeName: String) :
    CustomArgument<SurfPlayer, String>(StringArgument(nodeName), { info ->
        visibility.findOnlinePlayer(info.sender.asViewer(), info.input)
            ?: failCommand(CommandMessages.PLAYER_NOT_FOUND)
    }) {
    init {
        replaceSuggestions(ArgumentSuggestions.stringCollection { info ->
            visibility.onlinePlayerNames(info.sender.asViewer(), info.currentArg.orEmpty())
        })
    }
}

/**
 * Any player who has ever joined, resolved to their [SurfPlayer] (`null` when unknown). Suggests
 * the online players the sender may see, then known offline names; a hidden player only appears
 * among the offline names. Resolving never depends on whether the player is online.
 */
class VisibleOfflinePlayerArgument(nodeName: String) :
    CustomArgument<Deferred<SurfPlayer?>, String>(StringArgument(nodeName), { info ->
        val name = info.input
        plugin.scope.future(Dispatchers.IO) { SurfCoreApi.getOfflinePlayer(name) }.asDeferred()
    }) {
    init {
        replaceSuggestions(ArgumentSuggestions.stringCollectionAsync { info ->
            val viewer = info.sender.asViewer()
            val input = info.currentArg.orEmpty()
            plugin.scope.future(Dispatchers.IO) { visibility.offlinePlayerNames(viewer, input) }
        })
    }
}

/**
 * A player on this server, resolved to the Bukkit [Player]. Suggests and accepts only players the
 * sender may see; anybody else fails with [CommandMessages.PLAYER_NOT_FOUND].
 */
class VisibleLocalPlayerArgument(nodeName: String) :
    CustomArgument<Player, String>(StringArgument(nodeName), { info ->
        Bukkit.getPlayerExact(info.input)
            ?.takeIf { visibility.canSee(info.sender.asViewer(), it.uniqueId) }
            ?: failCommand(CommandMessages.PLAYER_NOT_FOUND)
    }) {
    init {
        replaceSuggestions(ArgumentSuggestions.stringCollection { info ->
            val viewer = info.sender.asViewer()
            val input = info.currentArg.orEmpty()
            Bukkit.getOnlinePlayers()
                .filter { it.name.startsWith(input, ignoreCase = true) && visibility.canSee(viewer, it.uniqueId) }
                .map { it.name }
        })
    }
}

fun CommandAPICommand.visiblePlayerArgument(nodeName: String, optional: Boolean = false): CommandAPICommand =
    withArguments(VisiblePlayerArgument(nodeName).setOptional(optional))

fun CommandAPICommand.visibleOfflinePlayerArgument(nodeName: String, optional: Boolean = false): CommandAPICommand =
    withArguments(VisibleOfflinePlayerArgument(nodeName).setOptional(optional))

fun CommandAPICommand.visibleLocalPlayerArgument(nodeName: String, optional: Boolean = false): CommandAPICommand =
    withArguments(VisibleLocalPlayerArgument(nodeName).setOptional(optional))
