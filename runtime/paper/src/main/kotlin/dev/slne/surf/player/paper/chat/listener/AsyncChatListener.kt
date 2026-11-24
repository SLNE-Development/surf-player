package dev.slne.surf.player.paper.chat.listener

import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.api.core.messages.adventure.uuidOrNull
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.player.api.chat.message.MessageContext
import dev.slne.surf.player.api.chat.message.MessageData
import dev.slne.surf.player.api.chat.message.MessageType
import dev.slne.surf.player.api.chat.message.redirector.MessageRedirectorRegistry
import dev.slne.surf.player.core.client.chat.processor.runPostProcessors
import dev.slne.surf.player.core.client.chat.processor.runPreProcessors
import dev.slne.surf.player.paper.plugin
import io.papermc.paper.event.player.AsyncChatEvent
import kotlinx.coroutines.runBlocking
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.time.OffsetDateTime
import java.util.*

object AsyncChatListener : Listener {
    @EventHandler
    fun onAsyncChat(event: AsyncChatEvent) {
        val signedMessage = event.signedMessage()
        val data = MessageData(
            event.message(),
            UUID.randomUUID(),
            event.player.uniqueId,
            null,
            OffsetDateTime.now(),
            SurfCoreApi.getCurrentServerName(),
            signedMessage.signature(),
            MessageType.GLOBAL
        )

        // The event must be modified before this handler returns, so pre-processing blocks.
        val result = runBlocking {
            runPreProcessors(MessageContext(data, event.isCancelled, event.viewers()))
        }

        if (result.isCancelled) {
            event.isCancelled = true
        }

        event.renderer { _, _, _, viewer ->
            viewer.uuidOrNull()?.let { result.render(it, viewer) } ?: event.message()
        }

        val processedData = result.messageData
        val postContext = MessageContext(processedData, result.isCancelled, event.viewers())

        plugin.launch {
            runPostProcessors(postContext)

            MessageRedirectorRegistry.redirectors.forEach {
                it.redirectMessage(signedMessage, processedData)
            }
        }
    }
}
