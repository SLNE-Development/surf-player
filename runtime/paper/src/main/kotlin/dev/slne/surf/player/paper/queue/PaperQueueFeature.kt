package dev.slne.surf.player.paper.queue

import dev.slne.surf.api.paper.event.register
import dev.slne.surf.player.core.client.queue.ClientQueueInstance
import dev.slne.surf.player.paper.PaperPlayerFeature
import dev.slne.surf.player.paper.queue.commands.queueCommand
import dev.slne.surf.player.paper.queue.listener.PlayerKickedDueToFullServerListener

/**
 * The server queue (formerly the `surf-queue-paper` plugin).
 */
object PaperQueueFeature : PaperPlayerFeature {
    override val name = "queue"

    override suspend fun onLoad() {
        ClientQueueInstance.get().load()
    }

    override suspend fun onEnable() {
        ClientQueueInstance.get().enable()

        PlayerKickedDueToFullServerListener.register()
        queueCommand()
    }

    override suspend fun onDisable() {
        ClientQueueInstance.get().disable()
    }
}
