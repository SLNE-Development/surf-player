package dev.slne.surf.player.paper.queue

import com.google.auto.service.AutoService
import dev.slne.surf.player.core.client.queue.ClientQueueInstance
import dev.slne.surf.player.core.common.queue.QueueInstance
import dev.slne.surf.player.paper.queue.startup.PolarQueueStartHook
import org.bukkit.Bukkit

@AutoService(QueueInstance::class)
class PaperSurfQueueInstance : ClientQueueInstance() {
    override val dataPath get() = PaperQueueFeature.dataPath

    /**
     * Whether the server accepts players, so queued players may be sent here. With PolarLoader
     * installed that is once Polar has finished loading, otherwise once the plugin is enabled.
     */
    @Volatile
    override var isLoaded: Boolean = false
        private set

    private val polarInstalled get() = Bukkit.getPluginManager().getPlugin(POLAR_PLUGIN_NAME) != null

    override suspend fun load() {
        super.load()

        if (polarInstalled) {
            PolarQueueStartHook.onPolarEnabled { isLoaded = true }
        }
    }

    override suspend fun enable() {
        super.enable()

        if (!polarInstalled) {
            isLoaded = true
        }
    }

    private companion object {
        const val POLAR_PLUGIN_NAME = "PolarLoader"
    }
}
