package dev.slne.surf.social.core.client.chat.redis.rpc

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import java.time.Instant
import java.util.*

@Serializable
data class SignedChatMessage(
    val sender: @Contextual UUID,
    val sessionId: @Contextual UUID,
    val index: Int,
    val signature: ByteArray?,
    val content: String,
    val timestamp: @Contextual Instant,
    val salt: Long,
    val lastSeen: List<ByteArray>,
    val unsignedContent: @Contextual Component?,
    val sessionKey: SessionKey?,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SignedChatMessage) return false

        if (index != other.index) return false
        if (salt != other.salt) return false
        if (sender != other.sender) return false
        if (sessionId != other.sessionId) return false
        if (!signature.contentEquals(other.signature)) return false
        if (content != other.content) return false
        if (timestamp != other.timestamp) return false
        if (lastSeen != other.lastSeen) return false
        if (unsignedContent != other.unsignedContent) return false
        if (sessionKey != other.sessionKey) return false

        return true
    }

    override fun hashCode(): Int {
        var result = index
        result = 31 * result + salt.hashCode()
        result = 31 * result + sender.hashCode()
        result = 31 * result + sessionId.hashCode()
        result = 31 * result + (signature?.contentHashCode() ?: 0)
        result = 31 * result + content.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + lastSeen.hashCode()
        result = 31 * result + unsignedContent.hashCode()
        result = 31 * result + sessionKey.hashCode()
        return result
    }
}
