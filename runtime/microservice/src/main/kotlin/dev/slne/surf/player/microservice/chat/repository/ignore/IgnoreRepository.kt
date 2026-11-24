package dev.slne.surf.player.microservice.chat.repository.ignore

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.Op
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.and
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.eq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.deleteWhere
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.insertIgnore
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.select
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.selectAll
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.player.api.chat.entry.IgnoreListEntry
import dev.slne.surf.player.microservice.chat.table.IgnoreListTable
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import java.util.*

object IgnoreRepository {
    /**
     * Returns `false` if [uuid] already ignores [ignoredUUID].
     *
     * ON CONFLICT DO NOTHING rather than catching the unique violation: PostgreSQL aborts the whole
     * transaction on a failed statement, so the following COMMIT would fail.
     */
    suspend fun ignore(uuid: UUID, ignoredUUID: UUID): Boolean = suspendTransaction {
        IgnoreListTable.insertIgnore {
            it[this.userUuid] = uuid
            it[this.ignoredUuid] = ignoredUUID
        }.insertedCount > 0
    }

    suspend fun unignore(uuid: UUID, ignoredUUID: UUID): Boolean = suspendTransaction {
        IgnoreListTable.deleteWhere { isIgnoringOp(uuid, ignoredUUID) } > 0
    }

    /** Whether [player] ignores [target]. */
    suspend fun isIgnoring(player: UUID, target: UUID): Boolean = suspendTransaction {
        IgnoreListTable.select(IgnoreListTable.id)
            .where { isIgnoringOp(player, target) }
            .limit(1)
            .firstOrNull() != null
    }

    private fun isIgnoringOp(player: UUID, target: UUID): Op<Boolean> =
        (IgnoreListTable.userUuid eq player) and (IgnoreListTable.ignoredUuid eq target)

    suspend fun findAllByUuid(uuid: UUID): List<IgnoreListEntry> = suspendTransaction {
        IgnoreListTable.selectAll()
            .where { IgnoreListTable.userUuid eq uuid }
            .map { row ->
                IgnoreListEntry(
                    user = row[IgnoreListTable.userUuid],
                    target = row[IgnoreListTable.ignoredUuid],
                    createdAt = row[IgnoreListTable.createdAt]
                )
            }
            .toList()
    }
}
