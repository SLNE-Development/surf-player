package dev.slne.surf.minimapcompanion.minestom.command

import dev.slne.surf.api.minestom.command.dsl.anyExecutor
import dev.slne.surf.api.minestom.command.dsl.commandTree
import dev.slne.surf.api.minestom.command.dsl.literalArgument
import dev.slne.surf.api.minestom.coroutine.minestomBlockingScope
import dev.slne.surf.minimapcompanion.core.command.sendReloadResult
import dev.slne.surf.minimapcompanion.core.config.CompanionConfigs
import dev.slne.surf.minimapcompanion.core.permission.CompanionPermissions
import dev.slne.surf.minimapcompanion.core.service.MapSyncService
import dev.slne.surf.minimapcompanion.minestom.platform.MinestomCompanionPlayer
import kotlinx.coroutines.launch
import net.minestom.server.MinecraftServer

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
                        // The player's instance is only safe to read in their own tick.
                        player.scheduler().scheduleNextTick {
                            val instance = player.instance ?: return@scheduleNextTick
                            MapSyncService.sync(MinestomCompanionPlayer(player, instance), result.settings)
                        }
                    }
                }

                sender.sendReloadResult(result)
            }
        }
    }
}
