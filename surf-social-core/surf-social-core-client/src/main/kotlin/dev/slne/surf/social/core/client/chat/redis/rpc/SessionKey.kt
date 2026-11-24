package dev.slne.surf.social.core.client.chat.redis.rpc

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.security.PublicKey
import java.time.Instant

/**
 * The chat session key a [SignedChatMessage] was signed with.
 */
@Serializable
data class SessionKey(
    val expiresAt: @Contextual Instant,
    val key: @Contextual PublicKey,
    val keySignature: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SessionKey) return false

        if (expiresAt != other.expiresAt) return false
        if (key != other.key) return false
        if (!keySignature.contentEquals(other.keySignature)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = expiresAt.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + keySignature.contentHashCode()
        return result
    }
}
