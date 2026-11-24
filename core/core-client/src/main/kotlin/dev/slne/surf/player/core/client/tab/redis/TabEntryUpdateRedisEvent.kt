package dev.slne.surf.player.core.client.tab.redis

import dev.slne.surf.redis.event.RedisEvent
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

/**
 * Asks every server in the network to re-render the tab entry of [toUpdateUuid].
 */
@Serializable
data class TabEntryUpdateRedisEvent(
    val toUpdateUuid: @Contextual UUID
) : RedisEvent()
