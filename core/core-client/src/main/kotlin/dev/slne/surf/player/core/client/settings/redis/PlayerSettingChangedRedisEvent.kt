package dev.slne.surf.player.core.client.settings.redis

import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.redis.event.RedisEvent
import kotlinx.serialization.Serializable

/**
 * [playerUuid] saved [value] for the setting [settingName]. Every server (Paper, Minestom,
 * Velocity) that has the settings of that player cached updates its cache.
 */
@Serializable
data class PlayerSettingChangedRedisEvent(
    val playerUuid: SerializableUUID,
    val settingName: String,
    val value: String
) : RedisEvent()
