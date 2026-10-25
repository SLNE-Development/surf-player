package dev.slne.surf.minimapcompanion.minestom.listener

import dev.slne.surf.api.minestom.event.EventRegistrar
import dev.slne.surf.api.minestom.extension.addListener
import dev.slne.surf.minimapcompanion.core.service.XaeroSyncService
import dev.slne.surf.minimapcompanion.core.xaero.XaeroChannels
import dev.slne.surf.minimapcompanion.minestom.platform.MinestomCompanionPlayer
import dev.slne.surf.minimapcompanion.minestom.platform.RegisteredChannels
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.event.player.PlayerPluginMessageEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import net.minestom.server.timer.TaskSchedule

/**
 * Syncs players whenever their client could have lost or not yet received the Xaero data.
 */
object CompanionListener : EventRegistrar {
    private const val REGISTER_CHANNEL = "minecraft:register"

    override fun register(node: EventNode<Event>) {
        // Also fires during the configuration phase, before the player has an instance.
        node.addListener<PlayerPluginMessageEvent> { event ->
            if (event.identifier != REGISTER_CHANNEL) return@addListener

            val player = event.player
            val channels = RegisteredChannels.parseRegisterPayload(event.message)
            RegisteredChannels.register(player.uuid, channels)

            val instance = player.instance ?: return@addListener
            val xaeroChannels = channels.filter(XaeroChannels::isXaero)
            if (xaeroChannels.isNotEmpty()) {
                XaeroSyncService.sync(MinestomCompanionPlayer(player, instance), xaeroChannels)
            }
        }

        // Fires on the first spawn and on every instance change.
        node.addListener<PlayerSpawnEvent> { event ->
            val player = event.player
            XaeroSyncService.sync(MinestomCompanionPlayer(player, event.instance))
            if (!event.isFirstSpawn) return@addListener

            for (delay in XaeroSyncService.JOIN_RESEND_DELAYS) {
                player.scheduler().buildTask {
                    val instance = player.instance ?: return@buildTask
                    XaeroSyncService.sync(MinestomCompanionPlayer(player, instance))
                }.delay(TaskSchedule.millis(delay.inWholeMilliseconds)).schedule()
            }
        }

        node.addListener<PlayerDisconnectEvent> { event ->
            val uuid = event.player.uuid
            RegisteredChannels.forget(uuid)
            XaeroSyncService.forget(uuid)
        }
    }
}
