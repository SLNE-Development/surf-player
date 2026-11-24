package dev.slne.surf.player.paper.chat.service

import com.github.shynixn.mccoroutine.folia.scope
import dev.slne.surf.api.core.messages.adventure.text
import dev.slne.surf.api.paper.extensions.server
import dev.slne.surf.api.paper.nms.NmsUseWithCaution
import dev.slne.surf.api.paper.nms.bridges.SurfPaperNmsPlayerBridge
import dev.slne.surf.api.paper.nms.bridges.packets.player.SurfPaperNmsPlayerPackets
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.redis.request.RequestTimeoutException
import dev.slne.surf.player.core.client.chat.redis.rpc.SendSignedMessageHandledRedisResponse
import dev.slne.surf.player.core.client.chat.redis.rpc.SendSignedMessageRedisRequest
import dev.slne.surf.player.core.client.chat.redis.rpc.SignedChatMessage
import dev.slne.surf.player.core.client.SurfPlayerClient
import dev.slne.surf.player.paper.chat.redis.rpc.chatSession
import dev.slne.surf.player.paper.chat.redis.rpc.toMirror
import dev.slne.surf.player.paper.chat.redis.rpc.toWire
import dev.slne.surf.player.paper.plugin
import net.kyori.adventure.chat.SignedMessage
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import java.util.*

/**
 * Delivers signed chat messages with changed content, locally or via Redis to the
 * server the target is connected to.
 */
@OptIn(NmsUseWithCaution::class)
object SignedMessageSender {
    fun sendLocalSignedMessage(
        sender: Player,
        target: Player,
        component: Component,
        signedMessage: SignedMessage,
    ) {
        SurfPaperNmsPlayerBridge.sendSignedMessageWithChangedContent(
            target,
            signedMessage,
            SurfPaperNmsPlayerBridge.getPaperRawChatType().bind(sender.displayName()),
            component
        )
    }

    fun sendRemoteSignedMessage(
        sender: Player,
        targetUuid: UUID,
        component: Component,
        signedMessage: SignedMessage,
    ) {
        if (SurfCoreApi.getPlayer(targetUuid) == null) {
            return
        }

        forwardToRemoteServer(sender, signedMessage, component) { wireMessage ->
            SurfPlayerClient.redisApi.sendRequest<SendSignedMessageHandledRedisResponse>(
                SendSignedMessageRedisRequest(
                    sender.uniqueId,
                    sender.name,
                    targetUuid,
                    wireMessage
                ),
            )
        }
    }

    /**
     * Serializes [signedMessage] with [component] as content and passes it to [sendRequest]
     * on the sender's chat message chain, so the signature chain stays intact.
     *
     * A request timeout means no server handled the message (the target went offline);
     * that is an expected outcome and therefore ignored.
     */
    fun forwardToRemoteServer(
        sender: Player,
        signedMessage: SignedMessage,
        component: Component,
        sendRequest: suspend (SignedChatMessage) -> Unit,
    ) {
        val messageMirror = requireNotNull(
            SurfPaperNmsPlayerBridge.createPlayerChatMessageMirrorFromAdventure(signedMessage, component)
        ) { "Failed to create message mirror." }

        SurfPaperNmsPlayerBridge.runOnChatMessageChain(sender, plugin.scope) {
            val session = SurfPaperNmsPlayerBridge.getRemoteChatSessionData(sender)

            try {
                sendRequest(messageMirror.toWire(session))
            } catch (_: RequestTimeoutException) {
            }
        }
    }

    fun handleRemoteSignedMessage(
        sender: UUID,
        senderName: String,
        target: UUID,
        signedChatMessage: SignedChatMessage
    ): Boolean {
        val targetPlayer = Bukkit.getPlayer(target) ?: return false

        showRemoteSignedMessage(targetPlayer, sender, senderName, signedChatMessage)

        return true
    }

    /**
     * Shows a signed message from a player on another server to [target]. The sender is
     * temporarily added to the target's player list so the client can verify the signature.
     */
    fun showRemoteSignedMessage(
        target: Player,
        sender: UUID,
        senderName: String,
        signedChatMessage: SignedChatMessage
    ) {
        val message =
            SurfPaperNmsPlayerBridge.createAdventureChatMessageFromMirror(signedChatMessage.toMirror())

        SurfPaperNmsPlayerPackets.createNewPlayerInfoUpdate(
            sender,
            server.createProfile(sender, senderName).also { it.completeFromCache() },
            false,
            30,
            GameMode.ADVENTURE,
            null,
            false,
            1,
            signedChatMessage.chatSession()
        ).execute(target)

        SurfPaperNmsPlayerBridge.sendPlayerChatMessage(
            target,
            message,
            SurfPaperNmsPlayerBridge.getPaperRawChatType().bind(text(senderName))
        )

        SurfPaperNmsPlayerPackets.removePlayerInfoUpdate(listOf(sender)).execute(target)
    }
}
