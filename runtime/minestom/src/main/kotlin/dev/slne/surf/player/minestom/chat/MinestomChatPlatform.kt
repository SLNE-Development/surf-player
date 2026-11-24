package dev.slne.surf.player.minestom.chat

import com.google.auto.service.AutoService
import dev.slne.surf.api.core.messages.adventure.sound
import dev.slne.surf.api.core.util.logger
import dev.slne.surf.api.minestom.chat.deleteSignedMessage
import dev.slne.surf.api.minestom.chat.sendSignedMessage
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.player.minestom.chat.player.boundDisplayName
import dev.slne.surf.player.minestom.chat.service.SignedMessageService
import dev.slne.surf.api.minestom.coroutine.minestomAsyncScope
import dev.slne.surf.api.minestom.extension.ConnectionManager
import dev.slne.surf.api.minestom.permission.hasPermission
import dev.slne.surf.player.core.client.chat.platform.ChatPlatform
import dev.slne.surf.player.core.client.chat.platform.receivesSignedMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.kyori.adventure.chat.SignedMessage
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.minestom.server.adventure.audience.Audiences
import net.minestom.server.sound.SoundEvent
import net.minestom.server.sound.SoundEventKeys
import java.util.UUID

@AutoService(ChatPlatform::class)
class MinestomChatPlatform : ChatPlatform {
    override fun hasPermission(uuid: UUID, permission: String) =
        ConnectionManager.getOnlinePlayerByUuid(uuid)?.hasPermission(permission) ?: false

    override fun sendMessage(uuid: UUID, message: Component) {
        ConnectionManager.getOnlinePlayerByUuid(uuid)?.sendMessage(message)
    }

    override fun broadcast(message: Component) {
        Audiences.players().sendMessage(message)
        Audiences.console().sendMessage(message)
    }

    override fun broadcast(message: Component, permission: String) {
        Audiences.players { it.hasPermission(permission) }
            .sendMessage(message)
        Audiences.console().sendMessage(message)
    }

    override fun deleteMessage(signature: SignedMessage.Signature) {
        ConnectionManager.onlinePlayers.forEach { it.deleteSignedMessage(signature) }
    }

    override fun onlinePlayerCount() = ConnectionManager.onlinePlayerCount

    override fun isOnline(uuid: UUID) = ConnectionManager.getOnlinePlayerByUuid(uuid) != null

    override fun playPingSound(uuid: UUID) {
        val player = ConnectionManager.getOnlinePlayerByUuid(uuid) ?: return

        player.playSound(PING_SOUND, Sound.Emitter.self())
    }

    override fun teleportToPlayer(who: UUID, target: UUID) {
        val whoPlayer = ConnectionManager.getOnlinePlayerByUuid(who) ?: return
        val targetPlayer = ConnectionManager.getOnlinePlayerByUuid(target) ?: return

        whoPlayer.teleport(targetPlayer.position)
    }

    override fun launchAsync(block: suspend CoroutineScope.() -> Unit) {
        minestomAsyncScope.launch { block() }
    }

    override suspend fun sendSignedMessage(
        senderUuid: UUID,
        targetUuid: UUID,
        signedMessage: SignedMessage,
        contentComponent: Component,
        requiredSetting: String?
    ) {
        val sender = ConnectionManager.getOnlinePlayerByUuid(senderUuid)

        if (sender == null) {
            log.atWarning()
                .log("Tried to send signed message from offline player $senderUuid to $targetUuid!")
            return
        }

        val target = ConnectionManager.getOnlinePlayerByUuid(targetUuid)

        if (target != null) {
            if (!receivesSignedMessage(targetUuid, requiredSetting)) return
            target.sendSignedMessage(signedMessage, sender.boundDisplayName(), contentComponent)
            return
        }

        if (SurfCoreApi.getPlayer(targetUuid) == null) {
            log.atWarning()
                .log("Tried to send signed message from $senderUuid to offline player $targetUuid!")
            return
        }

        SignedMessageService.sendRemoteSignedMessage(
            sender,
            targetUuid,
            contentComponent,
            signedMessage,
            requiredSetting
        )
    }

    companion object {
        private val log = logger()

        private val PING_SOUND = sound {
            type(SoundEvent.ENTITY_CHICKEN_EGG)
            source(Sound.Source.PLAYER)
        }
    }
}
