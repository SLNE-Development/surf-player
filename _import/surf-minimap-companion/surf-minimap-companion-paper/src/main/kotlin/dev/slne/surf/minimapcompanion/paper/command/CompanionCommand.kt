package dev.slne.surf.minimapcompanion.paper.command

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.slne.surf.minimapcompanion.core.permission.CompanionPermissions
import dev.slne.surf.minimapcompanion.core.command.sendReloadResult
import dev.slne.surf.minimapcompanion.core.config.CompanionConfigs
import dev.slne.surf.minimapcompanion.core.service.MapSyncService
import dev.slne.surf.minimapcompanion.paper.platform.PaperCompanionPlayer
import dev.slne.surf.minimapcompanion.paper.plugin
import dev.slne.surf.minimapcompanion.paper.tracker.PaperTracker
import org.bukkit.Bukkit

fun companionCommand() = commandTree("minimapcompanion") {
    withPermission(CompanionPermissions.COMMAND)

    literalArgument("reload") {
        withPermission(CompanionPermissions.COMMAND_RELOAD)
        anyExecutor { sender, _ ->
            val result = CompanionConfigs.reload()

            if (result is CompanionConfigs.ReloadResult.Reloaded) {
                PaperTracker.configure()

                for (player in Bukkit.getOnlinePlayers()) {
                    player.scheduler.run(plugin, {
                        MapSyncService.sync(PaperCompanionPlayer(player), result.settings)
                    }, null)
                }
            }

            sender.sendReloadResult(result)
        }
    }
}
