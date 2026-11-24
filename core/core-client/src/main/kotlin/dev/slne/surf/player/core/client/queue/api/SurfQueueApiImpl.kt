package dev.slne.surf.player.core.client.queue.api

import com.google.auto.service.AutoService
import dev.slne.surf.player.api.queue.SurfQueue
import dev.slne.surf.player.api.queue.SurfQueueApi
import dev.slne.surf.player.api.queue.SurfQueueAvailableSlotsProvider
import dev.slne.surf.player.core.client.queue.queue.DefaultSurfQueueAvailableSlotsProvider
import dev.slne.surf.player.core.common.queue.queue.RedisQueueService
import net.kyori.adventure.util.Services

/**
 * Exposes the queues of [RedisQueueService] as the public [SurfQueueApi].
 */
@AutoService(SurfQueueApi::class)
class SurfQueueApiImpl : SurfQueueApi, Services.Fallback {
    override fun queue(serverName: String): SurfQueue = RedisQueueService.getQueueByName(serverName)

    @Volatile
    override var availableSlotsProvider: SurfQueueAvailableSlotsProvider =
        DefaultSurfQueueAvailableSlotsProvider
}
