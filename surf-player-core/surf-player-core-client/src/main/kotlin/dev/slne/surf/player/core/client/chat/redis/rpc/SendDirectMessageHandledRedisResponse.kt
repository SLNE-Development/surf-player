package dev.slne.surf.social.core.client.chat.redis.rpc

import dev.slne.surf.redis.request.RedisResponse
import kotlinx.serialization.Serializable

/**
 * Confirms that a [SendDirectMessageRedisRequest] was handled by the server hosting the receiver.
 */
@Serializable
class SendDirectMessageHandledRedisResponse : RedisResponse()
