package dev.slne.surf.social.microservice.chat.repository.ignore

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.and
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.eq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.deleteWhere
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.insertIgnore
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.selectAll
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.social.api.chat.entry.IgnoreListEntry
import dev.slne.surf.social.microservice.chat.table.IgnoreListTable
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
        IgnoreListTable.deleteWhere {
            (IgnoreListTable.userUuid eq uuid) and (IgnoreListTable.ignoredUuid eq ignoredUUID)
        } > 0
    }

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
