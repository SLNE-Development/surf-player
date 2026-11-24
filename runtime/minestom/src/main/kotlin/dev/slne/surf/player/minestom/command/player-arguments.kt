package dev.slne.surf.player.minestom.command

import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.argument.CustomArgument
import dev.slne.surf.api.minestom.command.argument.StringArgument
import dev.slne.surf.api.minestom.command.suggestion.ArgumentSuggestions
import dev.slne.surf.api.minestom.permission.hasPermission
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.player.core.client.command.CommandMessages
import dev.slne.surf.player.core.client.util.luckPermsGrantedPermissions
import dev.slne.surf.player.core.client.vanish.CommandViewer
import dev.slne.surf.player.core.client.vanish.PlayerVisibility
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandSender
import net.minestom.server.entity.Player

/*
 * Every player argument of the Minestom commands. Suggestions and resolution go through
 * PlayerVisibility, so a vanished player is neither suggested to nor resolved for a sender that may
 * not see them; such a sender gets the same answer as for an offline or unknown player.
 */

private val visibility get() = PlayerVisibility.network

/** This sender as seen by [PlayerVisibility]: a player, or the console for everything else. */
fun CommandSender.asViewer(): CommandViewer =
    if (this is Player) {
        CommandViewer(uuid, { hasPermission(it) }, { luckPermsGrantedPermissions(uuid) })
    } else {
        CommandViewer.CONSOLE
    }

/**
 * A player online anywhere in the network, resolved to their [SurfPlayer]. Suggests and accepts
 * only players the sender may see; anybody else fails with [CommandMessages.PLAYER_NOT_FOUND].
 */
class VisiblePlayerArgument(nodeName: String) :
    CustomArgument<SurfPlayer, String>(StringArgument(nodeName), { info ->
        visibility.findOnlinePlayer(info.sender.asViewer(), info.currentInput)
            ?: failCommand(CommandMessages.PLAYER_NOT_FOUND)
    }) {
    init {
        replaceSuggestions(ArgumentSuggestions.stringCollection { info ->
            visibility.onlinePlayerNames(info.sender.asViewer(), info.currentArg)
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
        val name = info.currentInput
        commandArgumentScope.async { SurfCoreApi.getOfflinePlayer(name) }
    }) {
    init {
        replaceSuggestions(ArgumentSuggestions.stringCollectionAsync { info ->
            visibility.offlinePlayerNames(info.sender.asViewer(), info.currentArg)
        })
    }
}

/**
 * A player on this server, resolved to the Minestom [Player]. Suggests and accepts only players
 * the sender may see; anybody else fails with [CommandMessages.PLAYER_NOT_FOUND].
 */
class VisibleLocalPlayerArgument(nodeName: String) :
    CustomArgument<Player, String>(StringArgument(nodeName), { info ->
        MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(info.currentInput)
            ?.takeIf { visibility.canSee(info.sender.asViewer(), it.uuid) }
            ?: failCommand(CommandMessages.PLAYER_NOT_FOUND)
    }) {
    init {
        replaceSuggestions(ArgumentSuggestions.stringCollection { info ->
            val viewer = info.sender.asViewer()
            MinecraftServer.getConnectionManager().onlinePlayers
                .filter { it.username.startsWith(info.currentArg, ignoreCase = true) && visibility.canSee(viewer, it.uuid) }
                .map { it.username }
        })
    }
}

fun CommandAPICommand.visiblePlayerArgument(nodeName: String, optional: Boolean = false): CommandAPICommand =
    withArguments(VisiblePlayerArgument(nodeName).setOptional(optional = optional))

fun CommandAPICommand.visibleOfflinePlayerArgument(nodeName: String, optional: Boolean = false): CommandAPICommand =
    withArguments(VisibleOfflinePlayerArgument(nodeName).setOptional(optional = optional))

fun CommandAPICommand.visibleLocalPlayerArgument(nodeName: String, optional: Boolean = false): CommandAPICommand =
    withArguments(VisibleLocalPlayerArgument(nodeName).setOptional(optional = optional))
