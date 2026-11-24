package dev.slne.surf.player.core.client.chat.redis.rpc

import dev.slne.surf.redis.request.RedisRequest
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

/**
 * Asks the server the target of a signed message is on to show [message] to them.
 *
 * The request is published to every server; only the one hosting the target answers with
 * [SendSignedMessageHandledRedisResponse].
 *
 * [senderName] is needed because the receiving server has to introduce the sender to the client
 * before it accepts the signed message.
 *
 * With [requiredSetting] (the name of a boolean setting, e.g. `clan:chat-messages`) the target's
 * server answers but does not show the message if the target turned that setting off.
 */
@Serializable
data class SendSignedMessageRedisRequest(
    val sender: @Contextual UUID,
    val senderName: String,
    val target: @Contextual UUID,
    val message: SignedChatMessage,
    val requiredSetting: String? = null
) : RedisRequest()
