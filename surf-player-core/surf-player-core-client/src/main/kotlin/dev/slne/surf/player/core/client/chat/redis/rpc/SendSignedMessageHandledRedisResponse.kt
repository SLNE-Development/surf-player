package dev.slne.surf.social.core.client.chat.redis.rpc

import dev.slne.surf.redis.request.RedisResponse
import kotlinx.serialization.Serializable

/**
 * Confirms that a [SendSignedMessageRedisRequest] was handled by the server hosting the target.
 */
@Serializable
class SendSignedMessageHandledRedisResponse : RedisResponse()
