package dev.slne.surf.minimapcompanion.minestom.command

import dev.slne.surf.api.minestom.command.dsl.anyExecutor
import dev.slne.surf.api.minestom.command.dsl.commandTree
import dev.slne.surf.api.minestom.command.dsl.literalArgument
import dev.slne.surf.minimapcompanion.core.command.CompanionPermissions
import dev.slne.surf.minimapcompanion.core.command.sendReloadResult
import dev.slne.surf.minimapcompanion.core.config.CompanionConfigs
import dev.slne.surf.minimapcompanion.core.service.XaeroSyncService
import dev.slne.surf.minimapcompanion.minestom.platform.MinestomCompanionPlayer
import net.minestom.server.MinecraftServer

fun minimapCompanionCommand() = commandTree("minimapcompanion") {
    withPermission(CompanionPermissions.COMMAND)

    literalArgument("reload") {
        withPermission(CompanionPermissions.COMMAND_RELOAD)
        anyExecutor { sender, _ ->
            val result = CompanionConfigs.reload()

            if (result is CompanionConfigs.ReloadResult.Reloaded) {
                for (player in MinecraftServer.getConnectionManager().onlinePlayers) {
                    val instance = player.instance ?: continue
                    XaeroSyncService.sync(MinestomCompanionPlayer(player, instance), config = result.config, xaero = result.xaero)
                }
            }

            sender.sendReloadResult(result)
        }
    }
}
