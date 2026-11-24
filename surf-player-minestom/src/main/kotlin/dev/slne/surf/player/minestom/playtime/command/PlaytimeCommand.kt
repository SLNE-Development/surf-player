package dev.slne.surf.player.minestom.playtime.command

import dev.slne.surf.api.minestom.command.dsl.commandTree
import dev.slne.surf.api.minestom.command.dsl.playerExecutorSuspend
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.minestom.command.argument.surfOfflinePlayerArgument
import dev.slne.surf.player.core.client.playtime.command.sendPlayerNotFound
import dev.slne.surf.player.core.client.playtime.command.sendPlaytimeOverview
import dev.slne.surf.player.core.client.playtime.permission.PlaytimePermissions
import dev.slne.surf.player.core.common.playtime.service.PlaytimeService
import dev.slne.surf.player.core.common.playtime.service.PlaytimeStreakService
import kotlinx.coroutines.Deferred

fun playtimeCommand() = commandTree("playtime") {
    withAliases("pt")
    withPermission(PlaytimePermissions.COMMAND)

    playerExecutorSuspend { player, _ ->
        player.sendPlaytimeOverview(
            sessions = PlaytimeService.getAndLoadSessions(player.uuid),
            streak = PlaytimeStreakService.getStreak(player.uuid)
        )
    }

    surfOfflinePlayerArgument("player") {
        withPermission(PlaytimePermissions.COMMAND_OTHERS)

        playerExecutorSuspend { sender, args ->
            val player: Deferred<SurfPlayer?> by args

            val targetPlayer = player.await()
            if (targetPlayer == null) {
                sender.sendPlayerNotFound()
                return@playerExecutorSuspend
            }

            sender.sendPlaytimeOverview(
                sessions = PlaytimeService.getAndLoadSessions(targetPlayer.uuid),
                streak = PlaytimeStreakService.loadOrCalculateStreak(targetPlayer.uuid),
                targetName = targetPlayer.lastKnownName ?: "Unbekannt"
            )
        }
    }
}
