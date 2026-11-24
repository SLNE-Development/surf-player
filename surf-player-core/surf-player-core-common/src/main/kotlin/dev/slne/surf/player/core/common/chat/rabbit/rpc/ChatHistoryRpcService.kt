package dev.slne.surf.player.core.common.chat.rabbit.rpc

import dev.slne.surf.rabbitmq.api.rpc.RpcService
import dev.slne.surf.player.api.chat.entry.HistoryEntry
import dev.slne.surf.player.api.chat.entry.HistoryFilter
import java.time.OffsetDateTime
import java.util.*

/** The chat message history stored by the microservice. */
@RpcService
interface ChatHistoryRpcService {
    suspend fun createHistoryEntry(entry: HistoryEntry): Boolean

    /** The newest entries matching [filter], newest first. */
    suspend fun findHistories(filter: HistoryFilter): List<HistoryEntry>

    /** @return `false` if no entry with [messageUuid] exists */
    suspend fun markHistoryEntryDeleted(
        messageUuid: UUID,
        deletedBy: UUID?,
        deletionReason: String?,
        deletedAt: OffsetDateTime
    ): Boolean
}
