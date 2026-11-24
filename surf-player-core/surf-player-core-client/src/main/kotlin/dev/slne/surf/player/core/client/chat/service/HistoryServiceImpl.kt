package dev.slne.surf.player.core.client.chat.service

import com.google.auto.service.AutoService
import dev.slne.surf.api.core.messages.adventure.plain
import dev.slne.surf.api.core.util.toObjectList
import dev.slne.surf.player.api.chat.entry.HistoryEntry
import dev.slne.surf.player.api.chat.entry.HistoryFilter
import dev.slne.surf.player.api.chat.message.MessageData
import dev.slne.surf.player.core.client.chat.rpc.chatHistoryRpcService
import dev.slne.surf.player.core.common.chat.service.HistoryService
import it.unimi.dsi.fastutil.objects.ObjectList
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeout
import net.kyori.adventure.util.Services
import java.time.OffsetDateTime
import java.util.*
import kotlin.time.Duration.Companion.seconds

@AutoService(HistoryService::class)
class HistoryServiceImpl : HistoryService, Services.Fallback {
    private val loadHistorySemaphore = Semaphore(16)

    override suspend fun logMessage(messageData: MessageData) {
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

    override suspend fun findHistoryEntry(
        filter: HistoryFilter
    ): ObjectList<HistoryEntry> = withTimeout(10.seconds) {
        loadHistorySemaphore.withPermit {
            chatHistoryRpcService.findHistories(filter).toObjectList()
        }
    }

    override suspend fun availableLookups(): Int {
        return loadHistorySemaphore.availablePermits
    }

    override suspend fun markDeleted(
        messageUuid: UUID,
        deletedBy: UUID?,
        deletionReason: String?,
        deletedAt: OffsetDateTime
    ) {
        chatHistoryRpcService.markHistoryEntryDeleted(messageUuid, deletedBy, deletionReason, deletedAt)
    }
}
