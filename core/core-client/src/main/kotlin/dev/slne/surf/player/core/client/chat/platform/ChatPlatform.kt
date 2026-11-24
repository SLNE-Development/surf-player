package dev.slne.surf.player.core.client.chat.platform

import dev.slne.surf.api.core.util.requiredService
import kotlinx.coroutines.CoroutineScope
import net.kyori.adventure.chat.SignedMessage
import net.kyori.adventure.text.Component
import java.util.UUID

private val platform = requiredService<ChatPlatform>()

/**
 * Bridge to the platform specific server implementation used by the shared chat pipeline.
 */
interface ChatPlatform {
    fun hasPermission(uuid: UUID, permission: String): Boolean
    fun sendMessage(uuid: UUID, message: Component)
    fun broadcast(message: Component)
    fun broadcast(message: Component, permission: String)
    fun deleteMessage(signature: SignedMessage.Signature)
    fun onlinePlayerCount(): Int
    fun isOnline(uuid: UUID): Boolean
    fun playPingSound(uuid: UUID)
    fun teleportToPlayer(who: UUID, target: UUID)
    fun launchAsync(block: suspend CoroutineScope.() -> Unit)

    /**
     * Sends [signedMessage] from the player identified by [senderUuid], who must be on this
     * server, to the player identified by [targetUuid], on this or another server.
     */
    suspend fun sendSignedMessage(
        senderUuid: UUID,
        targetUuid: UUID,
        signedMessage: SignedMessage,
        contentComponent: Component
    )

    companion object : ChatPlatform by platform
}
