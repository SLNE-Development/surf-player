package dev.slne.surf.social.velocity.connection.redis

import dev.slne.surf.redis.libs.redisson.api.stream.StreamAddArgs
import dev.slne.surf.redis.libs.redisson.client.codec.StringCodec
import dev.slne.surf.social.core.client.SocialClient
import kotlinx.coroutines.future.await

/**
 * Asks surf-discord to grant or revoke the Discord roles of linked accounts. Uses the shared
 * surf-social Redis connection owned by [SocialClient].
 */
object DiscordRolesPublisher {
    /**
     * Shared with surf-discord and other services; changing it breaks them.
     */
    private const val STREAM_NAME = "surf-discord:discord-roles:whitelisted-roles"
    private const val STREAM_FIELD = "request"
    private const val MAX_STREAM_LENGTH = 10_000

    private val redisApi get() = SocialClient.redisApi

    private val stream by lazy {
        redisApi.redisson.getStream<String, String>(STREAM_NAME, StringCodec.INSTANCE)
    }

    suspend fun publish(action: DiscordRoleAction, discordUserId: Long) {
        val payload = redisApi.json.encodeToString(DiscordRoleRequest(action, discordUserId))

        stream.addAsync(
            StreamAddArgs.entry(STREAM_FIELD, payload)
                .trimNonStrict()
                .maxLen(MAX_STREAM_LENGTH)
                .noLimit()
        ).await()
    }
}
