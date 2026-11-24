package dev.slne.surf.player.minestom.command

import dev.slne.surf.api.minestom.command.dsl.anyExecutor
import dev.slne.surf.api.minestom.command.dsl.subcommand
import dev.slne.surf.api.minestom.coroutine.minestomBlockingScope
import dev.slne.surf.player.api.contentcreator.platform.PlatformState
import dev.slne.surf.player.core.client.command.CommandMessages
import dev.slne.surf.player.core.client.contentcreator.command.LiveStateDebugCommand
import dev.slne.surf.player.core.client.contentcreator.config.reloadContentCreatorConfig
import dev.slne.surf.player.core.client.contentcreator.permission.ContentCreatorPermissions
import dev.slne.surf.player.core.client.minimap.command.MapInfo
import dev.slne.surf.player.core.client.minimap.command.sendMapInfo
import dev.slne.surf.player.core.client.minimap.command.sendReloadResult
import dev.slne.surf.player.core.client.minimap.config.CompanionConfigs
import dev.slne.surf.player.core.client.minimap.config.ReloadResult
import dev.slne.surf.player.core.client.minimap.permission.MinimapPermissions
import dev.slne.surf.player.core.client.minimap.service.MapSyncService
import dev.slne.surf.player.core.client.nametag.command.NametagCommands
import dev.slne.surf.player.core.client.nametag.permission.NametagPermissions
import dev.slne.surf.player.core.client.tab.command.reloadTablist
import dev.slne.surf.player.core.client.tab.permission.TabPermissions
import dev.slne.surf.player.minestom.minimap.platform.MinestomMinimapPlayer
import kotlinx.coroutines.launch
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import kotlin.system.measureTimeMillis

/** The actions of `/surfplayer tab`. */
fun tabAdminActions() = listOf(
    MinestomAdminAction("reload", TabPermissions.ADMIN_RELOAD, "Lädt die Tablist-Konfiguration neu") {
        anyExecutor { sender, _ ->
            val millis = measureTimeMillis { reloadTablist() }
            sender.sendMessage(CommandMessages.configReloaded("tab", millis))
        }
    },
)

/** The actions of `/surfplayer nametag`. */
fun nametagAdminActions() = listOf(
    MinestomAdminAction("reload", NametagPermissions.ADMIN_RELOAD, "Lädt die Nametag-Konfiguration neu") {
        anyExecutor { sender, _ -> sender.sendMessage(NametagCommands.reload()) }
    },
)

/** The actions of `/surfplayer contentcreator`. */
fun contentCreatorAdminActions() = listOf(
    MinestomAdminAction("reload", ContentCreatorPermissions.ADMIN_RELOAD, "Lädt die Content-Creator-Konfiguration neu") {
        anyExecutor { sender, _ ->
            val millis = measureTimeMillis { reloadContentCreatorConfig() }
            sender.sendMessage(CommandMessages.configReloaded("contentcreator", millis))
        }
    },
    MinestomAdminAction(
        "debug",
        ContentCreatorPermissions.ADMIN_DEBUG,
        "Setzt deinen eigenen Twitch-Live-Status zum Testen",
        "<live|offline>"
    ) {
        withSubcommand(subcommand("live") {
            playerExecutorSuspend { player, _ -> LiveStateDebugCommand.setTwitchState(player, player.uuid, PlatformState.ONLINE) }
        })
        withSubcommand(subcommand("offline") {
            playerExecutorSuspend { player, _ -> LiveStateDebugCommand.setTwitchState(player, player.uuid, PlatformState.OFFLINE) }
        })
    },
)

/** The actions of `/surfplayer minimap`. */
fun minimapAdminActions() = listOf(
    MinestomAdminAction("reload", MinimapPermissions.ADMIN_RELOAD, "Lädt die Minimap-Konfiguration neu") {
        anyExecutor { sender, _ ->
            // Reading and writing the files would stall the tick thread the command runs on.
            minestomBlockingScope.launch {
                val result = CompanionConfigs.reload()

                if (result is ReloadResult.Reloaded) {
                    for (player in MinecraftServer.getConnectionManager().onlinePlayers) {
                        // The player's instance is only safe to read in their own tick.
                        player.scheduler().scheduleNextTick {
                            val instance = player.instance ?: return@scheduleNextTick
                            MapSyncService.sync(MinestomMinimapPlayer(player, instance))
                        }
                    }
                }

                sender.sendReloadResult(result)
            }
        }
    },
    MinestomAdminAction(
        "info",
        MinimapPermissions.ADMIN_INFO,
        "Zeigt, was ein Spieler auf diesem Server von den Karten-Mods bekommt",
        "<spieler>"
    ) {
        visibleLocalPlayerArgument("spieler")
        anyExecutor { sender, args ->
            val target: Player = args.get("spieler")
            target.scheduler().scheduleNextTick {
                val instance = target.instance ?: return@scheduleNextTick
                sender.sendMapInfo(MapInfo.of(MinestomMinimapPlayer(target, instance), target.username, CompanionConfigs.current))
            }
        }
    },
)
