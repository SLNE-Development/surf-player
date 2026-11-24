package dev.slne.surf.player.paper.chat

import com.github.shynixn.mccoroutine.folia.launch
import com.google.auto.service.AutoService
import dev.slne.surf.api.paper.extensions.server
import dev.slne.surf.player.core.client.chat.platform.ChatPlatform
import dev.slne.surf.player.paper.chat.util.schedulePingSound
import dev.slne.surf.player.paper.plugin
import kotlinx.coroutines.CoroutineScope
import net.kyori.adventure.chat.SignedMessage
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import java.util.UUID

@AutoService(ChatPlatform::class)
class PaperChatPlatform : ChatPlatform {
    override fun hasPermission(uuid: UUID, permission: String) =
        Bukkit.getPlayer(uuid)?.hasPermission(permission) ?: false

    override fun sendMessage(uuid: UUID, message: Component) {
        Bukkit.getPlayer(uuid)?.sendMessage(message)
    }

    override fun broadcast(message: Component) {
        server.broadcast(message)
    }

    override fun broadcast(message: Component, permission: String) {
        server.broadcast(message, permission)
    }

    override fun deleteMessage(signature: SignedMessage.Signature) {
        server.deleteMessage(signature)
    }

    override fun onlinePlayerCount() = Bukkit.getOnlinePlayers().size

    override fun isOnline(uuid: UUID) = Bukkit.getPlayer(uuid) != null

    override fun playPingSound(uuid: UUID) {
        Bukkit.getPlayer(uuid)?.schedulePingSound()
    }

    override fun teleportToPlayer(who: UUID, target: UUID) {
        val whoPlayer = Bukkit.getPlayer(who) ?: return
        val targetPlayer = Bukkit.getPlayer(target) ?: return

        whoPlayer.teleportAsync(targetPlayer.location)
    }

    override fun launchAsync(block: suspend CoroutineScope.() -> Unit) {
        plugin.launch { block() }
    }
}
