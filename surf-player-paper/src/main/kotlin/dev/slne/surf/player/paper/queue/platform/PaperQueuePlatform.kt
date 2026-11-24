package dev.slne.surf.social.paper.queue.platform

import com.github.shynixn.mccoroutine.folia.launch
import com.google.auto.service.AutoService
import dev.slne.surf.social.core.client.queue.platform.QueuePlatform
import dev.slne.surf.social.core.client.queue.platform.TransferKickReason
import dev.slne.surf.social.paper.queue.listener.PlayerKickedDueToFullServerListener
import dev.slne.surf.social.paper.queue.plugin
import kotlinx.coroutines.CoroutineScope
import org.bukkit.Bukkit
import java.util.*

@AutoService(QueuePlatform::class)
class PaperQueuePlatform : QueuePlatform {
    override fun maxPlayers() = Bukkit.getMaxPlayers()

    override fun onlinePlayerCount() = Bukkit.getOnlinePlayers().size

    override fun consumeKickReason(uuid: UUID) =
        PlayerKickedDueToFullServerListener.consumeKickReason(uuid)

    override fun launchAsync(block: suspend CoroutineScope.() -> Unit) = plugin.launch(block = block)
}
