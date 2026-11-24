package dev.slne.surf.social.minestom.queue

import dev.slne.surf.api.minestom.plugin.SurfMinestomPlugin
import dev.slne.surf.social.core.common.queue.QueueInstance
import dev.slne.surf.social.minestom.queue.command.queueCommand

object MinestomQueuePlugin : SurfMinestomPlugin() {
    override suspend fun onLoad() {
        QueueInstance.get().load()
    }

    override suspend fun onEnable() {
        QueueInstance.get().enable()
        queueCommand()
    }

    override suspend fun onDisable() {
        QueueInstance.get().disable()
    }
}
