package dev.slne.surf.player.microservice.chat.repository.history

import dev.slne.surf.player.api.chat.entry.HistoryEntry
import dev.slne.surf.player.api.chat.entry.HistoryFilter
import dev.slne.surf.player.microservice.chat.table.HistoryTable
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.*
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.andWhere
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.insert
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.selectAll
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.update
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import java.time.OffsetDateTime
import java.util.*

object HistoryRepository {
    suspend fun createHistoryEntry(
        entry: HistoryEntry
    ) = suspendTransaction {
        HistoryTable.insert {
            it[this.messageUuid] = entry.messageUuid
            it[this.senderUuid] = entry.senderUuid
            it[this.receiverUuid] = entry.receiverUuid
            it[this.message] = entry.message
            it[this.sentAt] = entry.sentAt
            it[this.type] = entry.messageType
            it[this.server] = entry.server
        }

        return@suspendTransaction true
    }

    suspend fun findHistories(filter: HistoryFilter): List<HistoryEntry> = suspendTransaction {
        val query = HistoryTable.selectAll()
            .limit(filter.limit)

        filter.senderUuid?.let { query.andWhere { HistoryTable.senderUuid eq it } }
        filter.receiverUuid?.let { query.andWhere { HistoryTable.receiverUuid eq it } }
        filter.messageType?.let { query.andWhere { HistoryTable.type eq it } }
        filter.after?.let { query.andWhere { HistoryTable.sentAt greaterEq it } }
        filter.deletedBy?.let { query.andWhere { HistoryTable.deletedBy eq it } }
        filter.deleted?.let {
            query.andWhere {
                if (it) {
                    HistoryTable.deletedAt neq null
                } else {
                    HistoryTable.deletedAt eq null
                }
            }
        }
        filter.server?.let { query.andWhere { HistoryTable.server eq it } }
        filter.messageUuid?.let { query.andWhere { HistoryTable.messageUuid eq it } }
        filter.message?.let { text ->
            query.andWhere { HistoryTable.message.lowerCase() like containsPattern(text.lowercase()) }
        }

        query
            .orderBy(HistoryTable.sentAt, SortOrder.DESC)
            .map(::historyEntryFromRow)
            .toList()
    }

    suspend fun markDeleted(
        messageUuid: UUID,
        deletedBy: UUID?,
        deletionReason: String?,
        deletedAt: OffsetDateTime
    ) = suspendTransaction {
        HistoryTable.update({ HistoryTable.messageUuid eq messageUuid }) {
            it[this.deletedBy] = deletedBy
            it[this.deletionReason] = deletionReason
            it[this.deletedAt] = deletedAt
        } > 0
    }

    /**
     * A LIKE pattern matching every text that contains [text] literally; `%`, `_` and the escape
     * character in [text] match themselves. Combined with lowercasing both sides this is an ILIKE.
     */
    internal fun containsPattern(text: String): LikePattern {
        val escaped = buildString {
            for (char in text) {
                if (char == LIKE_ESCAPE || char == '%' || char == '_') append(LIKE_ESCAPE)
                append(char)
            }
        }

        return LikePattern("%$escaped%", LIKE_ESCAPE)
    }

    private const val LIKE_ESCAPE = '\\'

    private fun historyEntryFromRow(row: ResultRow) = HistoryEntry(
        messageUuid = row[HistoryTable.messageUuid],
        senderUuid = row[HistoryTable.senderUuid],
        receiverUuid = row[HistoryTable.receiverUuid],
        messageType = row[HistoryTable.type],
        message = row[HistoryTable.message],
        sentAt = row[HistoryTable.sentAt],
        server = row[HistoryTable.server],
        deleted = row[HistoryTable.deletedAt] != null,
        deletedAt = row[HistoryTable.deletedAt],
        deletedBy = row[HistoryTable.deletedBy],
        deletionReason = row[HistoryTable.deletionReason]
    )
}