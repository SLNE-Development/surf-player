package dev.slne.surf.social.paper.minimap.command

import com.github.shynixn.mccoroutine.folia.asyncDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.entitySelectorArgumentOnePlayer
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.slne.surf.social.core.client.minimap.command.MapInfo
import dev.slne.surf.social.core.client.minimap.command.sendMapInfo
import dev.slne.surf.social.core.client.minimap.command.sendReloadResult
import dev.slne.surf.social.core.client.minimap.config.CompanionConfigs
import dev.slne.surf.social.core.client.minimap.permission.CompanionPermissions
import dev.slne.surf.social.core.client.minimap.service.MapSyncService
import dev.slne.surf.social.paper.minimap.platform.PaperCompanionPlayer
import dev.slne.surf.social.paper.minimap.plugin
import dev.slne.surf.social.paper.minimap.tracker.PaperTracker
import org.bukkit.Bukkit
import org.bukkit.entity.Player

fun companionCommand() = commandTree("minimapcompanion") {
    withPermission(CompanionPermissions.COMMAND)

    literalArgument("reload") {
        withPermission(CompanionPermissions.COMMAND_RELOAD)
        anyExecutor { sender, _ ->
            // Reading and writing the files would stall the main or region thread the command runs on.
            plugin.launch(plugin.asyncDispatcher) {
                val result = CompanionConfigs.reload()

                if (result is CompanionConfigs.ReloadResult.Reloaded) {
                    PaperTracker.configure()

                    for (player in Bukkit.getOnlinePlayers()) {
                        // Reads the settings when it runs, so a later reload that overtook this one wins.
                        player.scheduler.run(plugin, { MapSyncService.sync(PaperCompanionPlayer(player)) }, null)
                    }
                }

                sender.sendReloadResult(result)
            }
        }
    }

    literalArgument("info") {
        withPermission(CompanionPermissions.COMMAND_INFO)
        entitySelectorArgumentOnePlayer("player") {
            anyExecutor { sender, args ->
                val target = args["player"] as Player
                // The player's channels, world and tracker state are read on their own thread.
                target.scheduler.run(plugin, {
                    val info = MapInfo.of(
                        PaperCompanionPlayer(target),
                        target.name,
                        CompanionConfigs.current,
                        PaperTracker.info(target),
                    )
                    sender.sendMapInfo(info)
                }, null)
            }
        }
    }
}
