package dev.slne.surf.player.core.common.clan.player

import dev.slne.surf.api.core.serializer.java.uuid.SerializableStringUUID
import kotlinx.serialization.Serializable

/**
 * The clan related data of a player, as stored by the microservice.
 */
@Serializable
data class ClanPlayerImpl(
    val ID: ULong,
    val uuid: SerializableStringUUID,
    val acceptsClanInvites: Boolean = true
)
