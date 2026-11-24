package dev.slne.surf.player.microservice.chat.rpc

import dev.slne.surf.player.api.chat.entry.HistoryEntry
import dev.slne.surf.player.api.chat.entry.HistoryFilter
import dev.slne.surf.player.core.common.chat.rpc.ChatHistoryRpcService
import dev.slne.surf.player.microservice.chat.repository.history.HistoryRepository
import java.time.OffsetDateTime
import java.util.*

object ChatHistoryRpcServiceImpl : ChatHistoryRpcService {
    override suspend fun createHistoryEntry(entry: HistoryEntry) {
        HistoryRepository.createHistoryEntry(entry)
    }

    override suspend fun findHistoryEntries(filter: HistoryFilter): List<HistoryEntry> =
        HistoryRepository.findHistories(filter)

    override suspend fun updateHistoryEntryDeleted(
        messageUuid: UUID,
        deletedBy: UUID?,
        deletionReason: String?,
        deletedAt: OffsetDateTime
    ) {
        HistoryRepository.markDeleted(
            messageUuid = messageUuid,
            deletedBy = deletedBy,
            deletionReason = deletionReason,
            deletedAt = deletedAt
        )
    }
}
