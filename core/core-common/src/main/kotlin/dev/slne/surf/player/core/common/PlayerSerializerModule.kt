package dev.slne.surf.player.core.common

import dev.slne.surf.player.core.common.clan.ClanCoreSerializerModule
import kotlinx.serialization.modules.SerializersModule

/**
 * Name of the single RabbitMQ connection (and Redis owner) shared by every surf-player feature.
 * The microservice and all clients must use the same name, because the server consumes requests
 * from the queue with this name.
 */
const val PLAYER_CONNECTION_NAME = "surf-player"

/**
 * Serializers required on the shared `surf-player` RabbitMQ connection. Both the microservice and
 * the clients must use this module so polymorphic payloads of every feature can be (de)serialized.
 */
object PlayerSerializerModule {
    val module = SerializersModule {
        include(ClanCoreSerializerModule.module)
    }
}
