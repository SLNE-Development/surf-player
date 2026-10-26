package dev.slne.surf.minimapcompanion.paper.command

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.slne.surf.minimapcompanion.core.command.CompanionPermissions
import dev.slne.surf.minimapcompanion.core.command.sendReloadResult
import dev.slne.surf.minimapcompanion.core.config.CompanionReload
import dev.slne.surf.minimapcompanion.core.service.XaeroSyncService
import dev.slne.surf.minimapcompanion.paper.platform.PaperCompanionPlayer
import dev.slne.surf.minimapcompanion.paper.plugin
import dev.slne.surf.minimapcompanion.paper.tracker.PaperTracker
import org.bukkit.Bukkit

fun minimapCompanionCommand() = commandTree("minimapcompanion") {
    withPermission(CompanionPermissions.COMMAND)

    literalArgument("reload") {
        withPermission(CompanionPermissions.COMMAND_RELOAD)
        anyExecutor { sender, _ ->
            val result = CompanionReload.reload()
            PaperTracker.configure()

            for (player in Bukkit.getOnlinePlayers()) {
                player.scheduler.run(plugin, {
                    XaeroSyncService.sync(PaperCompanionPlayer(player), config = result.config)
                }, null)
            }

            sender.sendReloadResult(result)
        }
    }
}
