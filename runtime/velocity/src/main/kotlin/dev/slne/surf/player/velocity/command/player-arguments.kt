package dev.slne.surf.player.velocity.command

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.slne.surf.core.api.velocity.command.argument.SurfOfflinePlayerArgument
import dev.slne.surf.player.core.client.util.luckPermsGrantedPermissions
import dev.slne.surf.player.core.client.vanish.CommandViewer
import dev.slne.surf.player.core.client.vanish.PlayerVisibility
import java.util.concurrent.CompletableFuture

/*
 * Every player argument of the Velocity commands. Suggestions go through PlayerVisibility, so a
 * vanished player is not suggested as online to a sender that may not see them.
 */

/** This source as seen by [PlayerVisibility]: a player, or the console for everything else. */
fun CommandSource.asViewer(): CommandViewer =
    if (this is Player) {
        CommandViewer(uniqueId, ::hasPermission) { luckPermsGrantedPermissions(uniqueId) }
    } else {
        CommandViewer.CONSOLE
    }

/**
 * Any player who has ever joined, resolved like surf-core's [SurfOfflinePlayerArgument]. Suggests
 * the online players the sender may see, then known offline names; a hidden player only appears
 * among the offline names. Resolving never depends on whether the player is online.
 */
class VisibleOfflinePlayerArgument(nodeName: String) : SurfOfflinePlayerArgument(nodeName) {
    init {
        replaceSuggestions(ArgumentSuggestions.stringCollectionAsync { info ->
            val viewer = info.sender.asViewer()
            val input = info.currentArg.orEmpty()
            CompletableFuture.supplyAsync { PlayerVisibility.network.offlinePlayerNames(viewer, input) }
        })
    }
}

fun CommandAPICommand.visibleOfflinePlayerArgument(nodeName: String, optional: Boolean = false): CommandAPICommand =
    withArguments(VisibleOfflinePlayerArgument(nodeName).setOptional(optional))
