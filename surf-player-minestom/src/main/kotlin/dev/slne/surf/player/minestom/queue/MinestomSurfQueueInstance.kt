package dev.slne.surf.social.minestom.queue

import com.google.auto.service.AutoService
import dev.slne.surf.social.core.client.queue.ClientQueueInstance
import dev.slne.surf.social.core.common.queue.QueueInstance
import java.nio.file.Path

@AutoService(QueueInstance::class)
class MinestomSurfQueueInstance : ClientQueueInstance() {
    override val dataPath: Path get() = MinestomQueuePlugin.dataDirectory

    @Volatile
    override var isLoaded: Boolean = false
        private set

    override suspend fun enable() {
        super.enable()
        isLoaded = true
    }
}
