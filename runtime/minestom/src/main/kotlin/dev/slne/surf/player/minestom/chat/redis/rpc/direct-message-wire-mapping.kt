package dev.slne.surf.player.minestom.chat.redis.rpc

import dev.slne.surf.api.minestom.chat.RemoteSignedMessage
import dev.slne.surf.player.core.client.chat.redis.rpc.SessionKey
import dev.slne.surf.player.core.client.chat.redis.rpc.SignedChatMessage
import net.minestom.server.crypto.ChatSession
import net.minestom.server.crypto.MessageSignature
import net.minestom.server.crypto.PlayerPublicKey

fun RemoteSignedMessage.toWire(session: ChatSession?) = SignedChatMessage(
    sender = sender,
    sessionId = sessionId,
    index = index,
    signature = signature?.signature(),
    content = content,
    timestamp = timestamp,
    salt = salt,
    lastSeen = lastSeen.map { it.signature() },
    unsignedContent = unsignedContent,
    sessionKey = session?.publicKey()?.let {
        SessionKey(it.expiresAt(), it.publicKey(), it.signature())
    }
)

fun SignedChatMessage.toRemote() = RemoteSignedMessage(
    sender = sender,
    sessionId = sessionId,
    index = index,
    signature = signature?.let(::MessageSignature),
    content = content,
    timestamp = timestamp,
    salt = salt,
    lastSeen = lastSeen.map(::MessageSignature),
    unsignedContent = unsignedContent
)

fun SignedChatMessage.chatSession() = sessionKey?.let {
    ChatSession(sessionId, PlayerPublicKey(it.expiresAt, it.key, it.keySignature))
}
