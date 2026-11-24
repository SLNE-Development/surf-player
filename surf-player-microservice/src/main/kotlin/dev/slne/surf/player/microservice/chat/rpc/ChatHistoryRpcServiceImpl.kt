package dev.slne.surf.social.microservice.chat.rpc

import dev.slne.surf.social.api.chat.entry.HistoryEntry
import dev.slne.surf.social.api.chat.entry.HistoryFilter
import dev.slne.surf.social.core.common.chat.rabbit.rpc.ChatHistoryRpcService
import dev.slne.surf.social.microservice.chat.repository.history.HistoryRepository
import java.time.OffsetDateTime
import java.util.*

object ChatHistoryRpcServiceImpl : ChatHistoryRpcService {
    override suspend fun createHistoryEntry(entry: HistoryEntry): Boolean =
        HistoryRepository.createHistoryEntry(entry)

    override suspend fun findHistories(filter: HistoryFilter): List<HistoryEntry> =
        HistoryRepository.findHistories(filter)

    override suspend fun markHistoryEntryDeleted(
        messageUuid: UUID,
        deletedBy: UUID?,
        deletionReason: String?,
        deletedAt: OffsetDateTime
    ): Boolean = HistoryRepository.markDeleted(
        messageUuid = messageUuid,
        deletedBy = deletedBy,
        deletionReason = deletionReason,
        deletedAt = deletedAt
    )
}
