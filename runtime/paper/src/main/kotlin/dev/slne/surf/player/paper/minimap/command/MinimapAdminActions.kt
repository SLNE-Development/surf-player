package dev.slne.surf.player.paper.minimap.command

import com.github.shynixn.mccoroutine.folia.asyncDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.entitySelectorArgumentOnePlayer
import dev.jorel.commandapi.kotlindsl.getValue
import dev.slne.surf.player.core.client.minimap.command.MapInfo
import dev.slne.surf.player.core.client.minimap.command.sendMapInfo
import dev.slne.surf.player.core.client.minimap.command.sendReloadResult
import dev.slne.surf.player.core.client.minimap.config.CompanionConfigs
import dev.slne.surf.player.core.client.minimap.config.ReloadResult
import dev.slne.surf.player.core.client.minimap.permission.MinimapPermissions
import dev.slne.surf.player.core.client.minimap.service.MapSyncService
import dev.slne.surf.player.paper.command.PaperAdminAction
import dev.slne.surf.player.paper.minimap.platform.PaperMinimapPlayer
import dev.slne.surf.player.paper.minimap.tracker.PaperTracker
import dev.slne.surf.player.paper.plugin
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * The actions of `/surfplayer minimap`.
 */
fun minimapAdminActions() = listOf(
    PaperAdminAction("reload", MinimapPermissions.ADMIN_RELOAD, "Lädt die Minimap-Konfiguration neu") {
        anyExecutor { sender, _ ->
            // Reading and writing the files would stall the main or region thread the command runs on.
            plugin.launch(plugin.asyncDispatcher) {
                val result = CompanionConfigs.reload()

                if (result is ReloadResult.Reloaded) {
                    PaperTracker.configure()

                    for (player in Bukkit.getOnlinePlayers()) {
                        // Reads the settings when it runs, so a later reload that overtook this one wins.
                        player.scheduler.run(plugin, { MapSyncService.sync(PaperMinimapPlayer(player)) }, null)
                    }
                }

                sender.sendReloadResult(result)
            }
        }
    },

    PaperAdminAction(
        "info",
        MinimapPermissions.ADMIN_INFO,
        "Zeigt, was ein Spieler auf diesem Server von den Karten-Mods bekommt",
        "<spieler>"
    ) {
        entitySelectorArgumentOnePlayer("spieler")
        anyExecutor { sender, args ->
            val spieler: Player by args
            // The player's channels, world and tracker state are read on their own thread.
            spieler.scheduler.run(plugin, {
                val info = MapInfo.of(
                    PaperMinimapPlayer(spieler),
                    spieler.name,
                    CompanionConfigs.current,
                    PaperTracker.info(spieler),
                )
                sender.sendMapInfo(info)
            }, null)
        }
    },
)
