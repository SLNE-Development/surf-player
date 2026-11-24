package dev.slne.surf.social.paper.playtime.command

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.paper.command.argument.surfOfflinePlayerArgument
import dev.slne.surf.social.core.client.playtime.command.sendPlayerNotFound
import dev.slne.surf.social.core.client.playtime.command.sendPlaytimeOverview
import dev.slne.surf.social.core.client.playtime.permission.PlaytimePermissions
import dev.slne.surf.social.core.common.playtime.service.PlaytimeService
import dev.slne.surf.social.core.common.playtime.service.PlaytimeStreakService
import dev.slne.surf.social.paper.playtime.plugin
import kotlinx.coroutines.Deferred

fun playtimeCommand() = commandTree("playtime") {
    withAliases("pt")
    withPermission(PlaytimePermissions.COMMAND)

    playerExecutor { player, _ ->
        plugin.launch {
            player.sendPlaytimeOverview(
                sessions = PlaytimeService.getAndLoadSessions(player.uniqueId),
                streak = PlaytimeStreakService.getStreak(player.uniqueId)
            )
        }
    }

    surfOfflinePlayerArgument("player") {
        withPermission(PlaytimePermissions.COMMAND_OTHERS)

        playerExecutor { sender, args ->
            val player: Deferred<SurfPlayer?> by args

            plugin.launch {
                val targetPlayer = player.await()
                if (targetPlayer == null) {
                    sender.sendPlayerNotFound()
                    return@launch
                }

                sender.sendPlaytimeOverview(
                    sessions = PlaytimeService.getAndLoadSessions(targetPlayer.uuid),
                    streak = PlaytimeStreakService.loadOrCalculateStreak(targetPlayer.uuid),
                    targetName = targetPlayer.lastKnownName ?: "Unbekannt"
                )
            }
        }
    }
}
