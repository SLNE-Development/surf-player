package dev.slne.surf.social.minestom.queue.platform

import com.google.auto.service.AutoService
import dev.slne.surf.api.minestom.coroutine.minestomAsyncScope
import dev.slne.surf.api.minestom.extension.ConnectionManager
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.social.core.client.queue.platform.QueuePlatform
import dev.slne.surf.social.core.client.queue.platform.TransferKickReason
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*

@AutoService(QueuePlatform::class)
class MinestomQueuePlatform : QueuePlatform {
    override fun maxPlayers() = SurfServer.current().maxPlayers

    override fun onlinePlayerCount() = ConnectionManager.onlinePlayerCount

    /**
     * Minestom exposes no rejection reason, so every refused connection is reported as
     * [TransferKickReason.OTHER].
     */
    override fun consumeKickReason(uuid: UUID) = TransferKickReason.OTHER

    override fun launchAsync(block: suspend CoroutineScope.() -> Unit) =
        minestomAsyncScope.launch(block = block)
}
