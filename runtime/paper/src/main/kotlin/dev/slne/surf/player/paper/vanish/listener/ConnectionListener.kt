package dev.slne.surf.player.paper.vanish.listener

import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.glow.SurfGlowingApi
import dev.slne.surf.api.paper.util.forEachPlayerInRegion
import dev.slne.surf.player.paper.plugin
import dev.slne.surf.player.paper.vanish.service.VANISH_GLOW_COLOR
import dev.slne.surf.player.paper.vanish.service.VanishService
import dev.slne.surf.player.core.client.vanish.permission.VanishPermissions
import dev.slne.surf.player.paper.vanish.util.canVanishSee
import dev.slne.surf.player.paper.vanish.util.isVanished
import io.papermc.paper.event.player.PlayerClientLoadedWorldEvent
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object ConnectionListener : Listener {
    private val pendingFlyRestore = ConcurrentHashMap.newKeySet<UUID>()

    @EventHandler
    fun onConnect(event: PlayerJoinEvent) {
        val player = event.player

        pendingFlyRestore.add(player.uniqueId)

        if (player.isVanished) {
            event.joinMessage(null)
            VanishService.refreshPriority(player)
        }

        VanishService.allOnline().forEach { vanishedPlayer ->
            if (!player.canVanishSee(vanishedPlayer)) {
                player.hidePlayer(plugin, vanishedPlayer)
            }
        }

        if (player.isVanished) {
            if (VanishService.isSpectating(player.uniqueId)) {
                VanishService.createAndShowScoreboard(player)
            }

            VanishService.current(player)?.player?.let { currentPlayer ->
                SurfGlowingApi.makeGlowing(currentPlayer, player, VANISH_GLOW_COLOR)
            }

            plugin.launch {
                forEachPlayerInRegion(plugin, { onlinePlayer ->
                    if (onlinePlayer.uniqueId == player.uniqueId) {
                        return@forEachPlayerInRegion
                    }

                    // Only players who may see vanished players learn about the vanished join.
                    if (!onlinePlayer.canVanishSee(player)) {
                        onlinePlayer.hidePlayer(plugin, player)
                    } else {
                        onlinePlayer.sendText {
                            appendInfoPrefix()
                            variableValue(player.name)
                            info(" hat den Server unsichtbar betreten.")
                        }
                    }
                }, true)
            }

            player.sendText {
                appendInfoPrefix()
                info("Du bist für andere Spieler unsichtbar.")
            }
        }
    }

    @EventHandler
    fun onClientLoaded(event: PlayerClientLoadedWorldEvent) {
        val player = event.player

        if (!pendingFlyRestore.remove(player.uniqueId)) {
            return
        }

        if (player.hasPermission(VanishPermissions.VANISH_SAVE_FLY_STATE)) {
            if (VanishService.getFlyState(player.uniqueId)) {
                player.allowFlight = true
                player.isFlying = true
            }
        }
    }

    @EventHandler
    fun onDisconnect(event: PlayerQuitEvent) {
        val player = event.player

        pendingFlyRestore.remove(player.uniqueId)
        VanishService.hideAndDeleteScoreboard(player)
        VanishService.setFlyState(player.uniqueId, player.isFlying)

        if (player.isVanished) {
            event.quitMessage(null)

            Bukkit.getOnlinePlayers()
                .filterNot { it.uniqueId == player.uniqueId }
                .forEach { onlinePlayer ->
                    if (onlinePlayer.canVanishSee(player)) {
                        onlinePlayer.sendText {
                            appendInfoPrefix()
                            variableValue(player.name)
                            info(" hat den Server unsichtbar verlassen.")
                        }
                    }
                }
        }
    }
}