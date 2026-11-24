package dev.slne.surf.player.core.client.friends.redis.event

import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.redis.event.RedisEvent
import kotlinx.serialization.Serializable

/**
 * Published by the proxy when [playerUuid] joins ([online] `true`) or leaves ([online] `false`) the
 * network. Server switches publish nothing, so friends are notified exactly once per network join
 * and quit, on every server.
 *
 * [serverName] is the first server the player connected to on join (`null` on quit); only that
 * server greets the player.
 */
@Serializable
data class FriendConnectionRedisEvent(
    val playerUuid: SerializableUUID,
    val online: Boolean,
    val serverName: String? = null,
) : RedisEvent()
