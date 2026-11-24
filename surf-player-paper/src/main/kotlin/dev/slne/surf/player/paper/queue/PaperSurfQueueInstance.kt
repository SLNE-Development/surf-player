package dev.slne.surf.player.paper.queue

import com.google.auto.service.AutoService
import dev.slne.surf.api.paper.event.register
import dev.slne.surf.player.core.client.queue.ClientQueueInstance
import dev.slne.surf.player.core.common.queue.QueueInstance
import dev.slne.surf.player.paper.queue.commands.queueCommand
import dev.slne.surf.player.paper.queue.hook.startup.QueueStartHook
import dev.slne.surf.player.paper.queue.listener.PlayerKickedDueToFullServerListener

@AutoService(QueueInstance::class)
class PaperSurfQueueInstance : ClientQueueInstance() {
    override val componentOwner get() = plugin
    override val dataPath get() = plugin.dataPath

    @Volatile
    override var isLoaded: Boolean = false
        private set

    override suspend fun load() {
        super.load()
        QueueStartHook.get().onServerReady {
            isLoaded = true
        }
    }

    override suspend fun enable() {
        super.enable()
        PlayerKickedDueToFullServerListener.register()
        queueCommand()
    }
}
