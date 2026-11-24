package dev.slne.surf.player.core.common.chat.rpc

import dev.slne.surf.rabbitmq.api.rpc.RpcService
import dev.slne.surf.player.api.chat.entry.HistoryEntry
import dev.slne.surf.player.api.chat.entry.HistoryFilter
import java.time.OffsetDateTime
import java.util.*

/** The chat message history stored by the microservice. */
@RpcService
interface ChatHistoryRpcService {
    suspend fun createHistoryEntry(entry: HistoryEntry)

    /** The newest entries matching [filter], newest first. */
    suspend fun findHistoryEntries(filter: HistoryFilter): List<HistoryEntry>

    /** Marks the entry of [messageUuid] as deleted; does nothing if there is no such entry. */
    suspend fun updateHistoryEntryDeleted(
        messageUuid: UUID,
        deletedBy: UUID?,
        deletionReason: String?,
        deletedAt: OffsetDateTime
    )
}
