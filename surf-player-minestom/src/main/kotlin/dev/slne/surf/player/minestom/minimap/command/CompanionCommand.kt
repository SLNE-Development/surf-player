package dev.slne.surf.player.minestom.minimap.command

import dev.slne.surf.api.minestom.command.dsl.anyExecutor
import dev.slne.surf.api.minestom.command.dsl.commandTree
import dev.slne.surf.api.minestom.command.dsl.literalArgument
import dev.slne.surf.api.minestom.command.dsl.playerArgument
import dev.slne.surf.api.minestom.coroutine.minestomBlockingScope
import dev.slne.surf.player.core.client.minimap.command.MapInfo
import dev.slne.surf.player.core.client.minimap.command.sendMapInfo
import dev.slne.surf.player.core.client.minimap.command.sendReloadResult
import dev.slne.surf.player.core.client.minimap.config.CompanionConfigs
import dev.slne.surf.player.core.client.minimap.permission.CompanionPermissions
import dev.slne.surf.player.core.client.minimap.service.MapSyncService
import dev.slne.surf.player.minestom.minimap.platform.MinestomCompanionPlayer
import kotlinx.coroutines.launch
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player

fun companionCommand() = commandTree("minimapcompanion") {
    withPermission(CompanionPermissions.COMMAND)

    literalArgument("reload") {
        withPermission(CompanionPermissions.COMMAND_RELOAD)
        anyExecutor { sender, _ ->
            // Reading and writing the files would stall the tick thread the command runs on.
            minestomBlockingScope.launch {
                val result = CompanionConfigs.reload()

                if (result is CompanionConfigs.ReloadResult.Reloaded) {
                    for (player in MinecraftServer.getConnectionManager().onlinePlayers) {
                        // The player's instance is only safe to read in their own tick. The settings are read then
                        // too, so a later reload that overtook this one wins.
                        player.scheduler().scheduleNextTick {
                            val instance = player.instance ?: return@scheduleNextTick
                            MapSyncService.sync(MinestomCompanionPlayer(player, instance))
                        }
                    }
                }

                sender.sendReloadResult(result)
            }
        }
    }

    literalArgument("info") {
        withPermission(CompanionPermissions.COMMAND_INFO)
        playerArgument("player") {
            anyExecutor { sender, args ->
                val target: Player = args.get("player")
                // The player's instance is only safe to read in their own tick. Minestom runs no player tracker.
                target.scheduler().scheduleNextTick {
                    val instance = target.instance ?: return@scheduleNextTick
                    sender.sendMapInfo(MapInfo.of(MinestomCompanionPlayer(target, instance), target.username, CompanionConfigs.current))
                }
            }
        }
    }
}
