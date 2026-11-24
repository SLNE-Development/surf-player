package dev.slne.surf.player.paper.queue

import dev.slne.surf.api.paper.event.register
import dev.slne.surf.player.core.client.queue.ClientQueueSystem
import dev.slne.surf.player.core.common.queue.QueueSystem
import dev.slne.surf.player.paper.PaperPlayerFeature
import dev.slne.surf.player.paper.queue.commands.queueCommand
import dev.slne.surf.player.paper.queue.listener.PlayerKickedDueToFullServerListener
import dev.slne.surf.player.paper.queue.startup.PolarQueueStartHook
import org.bukkit.Bukkit

/**
 * The server queue (formerly the `surf-queue-paper` plugin).
 *
 * The server accepts queued players ([QueueSystem.isLoaded]) once Polar has finished loading if
 * PolarLoader is installed, otherwise once the plugin is enabled.
 */
object PaperQueueFeature : PaperPlayerFeature {
    override val name = "queue"

    private const val POLAR_PLUGIN_NAME = "PolarLoader"

    private val polarInstalled get() = Bukkit.getPluginManager().getPlugin(POLAR_PLUGIN_NAME) != null

    override suspend fun onLoad() {
        ClientQueueSystem.load(dataPath)

        if (polarInstalled) {
            PolarQueueStartHook.onPolarEnabled { QueueSystem.isLoaded = true }
        }
    }

    override suspend fun onEnable() {
        ClientQueueSystem.enable()

        if (!polarInstalled) {
            QueueSystem.isLoaded = true
        }

        PlayerKickedDueToFullServerListener.register()
        queueCommand()
    }

    override suspend fun onDisable() {
        ClientQueueSystem.disable()
    }
}
