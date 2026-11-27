package dev.slne.surf.player.paper.chat.listener

import dev.slne.surf.api.paper.util.forEachPlayer
import dev.slne.surf.player.paper.chat.hook.SettingsHook
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerAdvancementDoneEvent

/**
 * Sends advancement messages only to the players whose advancement message setting allows them.
 * Runs last, so listeners that remove the message (such as vanish) have already done so.
 */
object AdvancementMessageListener : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onAdvancementDone(event: PlayerAdvancementDoneEvent) {
        val message = event.message() ?: return
        val achiever = event.player.uniqueId

        event.message(null)

        forEachPlayer { viewer ->
            val visibility = SettingsHook.advancementMessageVisibility(viewer.uniqueId)
            if (visibility.shows(ownAdvancement = viewer.uniqueId == achiever)) {
                viewer.sendMessage(message)
            }
        }

        Bukkit.getConsoleSender().sendMessage(message)
    }
}
