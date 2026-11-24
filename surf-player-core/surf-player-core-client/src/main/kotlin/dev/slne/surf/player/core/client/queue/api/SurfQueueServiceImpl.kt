package dev.slne.surf.player.core.client.queue.api

import com.google.auto.service.AutoService
import dev.slne.surf.player.api.queue.InternalSurfQueueApi
import dev.slne.surf.player.api.queue.service.SurfQueueService
import dev.slne.surf.player.core.common.queue.queue.RedisQueueService

/**
 * Exposes the [RedisQueueService] of the platform as the public [SurfQueueService].
 */
@OptIn(InternalSurfQueueApi::class)
@AutoService(SurfQueueService::class)
class SurfQueueServiceImpl : SurfQueueService by RedisQueueService
