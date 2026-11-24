package dev.slne.surf.player.core.client.chat.service

import dev.slne.surf.api.core.messages.adventure.plain
import dev.slne.surf.api.core.util.toObjectList
import dev.slne.surf.player.api.chat.entry.HistoryEntry
import dev.slne.surf.player.api.chat.entry.HistoryFilter
import dev.slne.surf.player.api.chat.message.MessageData
import dev.slne.surf.player.core.client.chat.rpc.chatHistoryRpcService
import it.unimi.dsi.fastutil.objects.ObjectList
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import java.time.OffsetDateTime
import java.util.*
import kotlin.time.Duration.Companion.seconds

/**
 * The chat history, stored by the microservice.
 */
object HistoryService {
    private val loadHistorySemaphore = Semaphore(16)

    /**
     * Logs the provided message data into the system's history.
     */
    suspend fun logMessage(messageData: MessageData) {
        chatHistoryRpcService.createHistoryEntry(
            HistoryEntry(
                messageUuid = messageData.messageUuid,
                senderUuid = messageData.sender,
                receiverUuid = messageData.receiver,
                messageType = messageData.type,
                sentAt = messageData.sentAt,
                message = messageData.message.plain(),
                server = messageData.server,
                deleted = false
            )
        )
    }

    /**
     * Finds chat history entries that match the provided filter criteria.
     */
    suspend fun findHistoryEntry(
        filter: HistoryFilter
    ): ObjectList<HistoryEntry> = withTimeout(10.seconds) {
        loadHistorySemaphore.withPermit {
            chatHistoryRpcService.findHistories(filter).toObjectList()
        }
    }

    /**
     * How many history lookups can currently run without waiting.
     */
    fun availableLookups(): Int {
        return loadHistorySemaphore.availablePermits
    }

    /**
     * Marks a message as deleted in the chat history.
     */
    suspend fun markDeleted(
        messageUuid: UUID,
        deletedBy: UUID?,
        deletionReason: String? = null,
        deletedAt: OffsetDateTime = OffsetDateTime.now()
    ) {
        chatHistoryRpcService.markHistoryEntryDeleted(messageUuid, deletedBy, deletionReason, deletedAt)
    }
}
