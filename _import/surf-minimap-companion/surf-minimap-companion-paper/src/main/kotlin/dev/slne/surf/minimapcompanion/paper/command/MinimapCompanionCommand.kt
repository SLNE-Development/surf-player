package dev.slne.surf.minimapcompanion.paper.command

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.slne.surf.minimapcompanion.core.command.CompanionPermissions
import dev.slne.surf.minimapcompanion.core.command.sendReloadResult
import dev.slne.surf.minimapcompanion.core.config.CompanionConfigs
import dev.slne.surf.minimapcompanion.core.service.MapSyncService
import dev.slne.surf.minimapcompanion.paper.platform.PaperCompanionPlayer
import dev.slne.surf.minimapcompanion.paper.plugin
import dev.slne.surf.minimapcompanion.paper.tracker.PaperTracker
import org.bukkit.Bukkit

fun minimapCompanionCommand() = commandTree("minimapcompanion") {
    withPermission(CompanionPermissions.COMMAND)

    literalArgument("reload") {
        withPermission(CompanionPermissions.COMMAND_RELOAD)
        anyExecutor { sender, _ ->
            val result = CompanionConfigs.reload()

            if (result is CompanionConfigs.ReloadResult.Reloaded) {
                PaperTracker.configure()

                for (player in Bukkit.getOnlinePlayers()) {
                    player.scheduler.run(plugin, {
                        MapSyncService.sync(
                            PaperCompanionPlayer(player),
                            config = result.config,
                            xaero = result.xaero,
                            journeyMap = result.journeyMap,
                            voxelMap = result.voxelMap,
                        )
                    }, null)
                }
            }

            sender.sendReloadResult(result)
        }
    }
}
