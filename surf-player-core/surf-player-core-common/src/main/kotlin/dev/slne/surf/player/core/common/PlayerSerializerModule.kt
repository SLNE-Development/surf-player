package dev.slne.surf.social.core.common

import dev.slne.surf.social.core.common.clan.ClanCoreSerializerModule
import kotlinx.serialization.modules.SerializersModule

/**
 * Name of the single RabbitMQ connection (and Redis owner) shared by every surf-social feature.
 * The microservice and all clients must use the same name, because the server consumes requests
 * from the queue with this name.
 */
const val SOCIAL_CONNECTION_NAME = "surf-social"

/**
 * Serializers required on the shared `surf-social` RabbitMQ connection. Both the microservice and
 * the clients must use this module so polymorphic payloads of every feature can be (de)serialized.
 */
object SocialSerializerModule {
    val module = SerializersModule {
        include(ClanCoreSerializerModule.module)
    }
}
