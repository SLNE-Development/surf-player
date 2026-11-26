package dev.slne.surf.player.paper.chat.listener

import dev.slne.surf.api.paper.util.forEachPlayer
import dev.slne.surf.player.paper.chat.hook.SettingsHook
import dev.slne.surf.player.paper.vanish.util.isVanished
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerAdvancementDoneEvent

object AdvancementMessageListener : Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onAdvancementDone(event: PlayerAdvancementDoneEvent) {
        val message = event.message() ?: return
        if (event.player.isVanished) {
            return
        }

        event.message(null)

        forEachPlayer { viewer ->
            if (SettingsHook.hasAdvancementsEnabled(viewer.uniqueId)) {
                viewer.sendMessage(message)
            }
        }

        Bukkit.getConsoleSender().sendMessage(message)
    }
}
