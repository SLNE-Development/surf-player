package dev.slne.surf.social.microservice.clan.db.migration

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.Table
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.vendors.PostgreSQLDialect
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.vendors.currentDialect
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.TransactionManager
import dev.slne.surf.social.microservice.clan.db.table.ClanInvitesTable
import dev.slne.surf.social.microservice.clan.db.table.ClanMembersTable
import dev.slne.surf.social.microservice.clan.db.table.ClanPlayerTable
import dev.slne.surf.social.microservice.clan.db.table.ClansTable

private data class UuidColumn(
    val table: Table,
    val column: String,
)

private val uuidColumns = listOf(
    UuidColumn(ClansTable, "uuid"),
    UuidColumn(ClansTable, "created_by"),
    UuidColumn(ClanMembersTable, "uuid"),
    UuidColumn(ClanMembersTable, "added_by"),
    UuidColumn(ClanPlayerTable, "uuid"),
    UuidColumn(ClanInvitesTable, "invited"),
    UuidColumn(ClanInvitesTable, "invited_by"),
)

/** Quotes [value] as a PostgreSQL string literal. */
private fun sqlLiteral(value: String) = "'" + value.replace("'", "''") + "'"

/**
 * Converts UUID columns created by the legacy MariaDB schema as text into native PostgreSQL UUID
 * columns. Exposed's SchemaUtils creates missing tables and columns, but it does not change the type
 * of an existing column.
 *
 * The clan tables are schema-qualified (see [dev.slne.surf.social.microservice.SocialSchemas]), so
 * the lookup and the `ALTER TABLE` use each table's own schema instead of `current_schema()`, which
 * is the connection's `SURF_DATABASE_SCHEMA` and no longer the clan schema.
 *
 * The migration is safe to execute on every startup. Columns that are already UUID are skipped. If
 * a legacy column contains a malformed UUID, PostgreSQL aborts startup instead of silently changing
 * or discarding data.
 */
suspend fun migrateLegacyClanUuidColumns() {
    if (currentDialect !is PostgreSQLDialect) return

    val transaction = TransactionManager.current()
    for ((table, column) in uuidColumns) {
        val schema = sqlLiteral(
            checkNotNull(table.schemaName) { "Clan table ${table.tableName} is not schema-qualified" }
        )
        val tableName = sqlLiteral(table.nameInDatabaseCaseUnquoted())
        val columnName = sqlLiteral(column)
        transaction.exec(
            """
            DO ${'$'}migration${'$'}
            DECLARE
                legacy_type text;
            BEGIN
                SELECT data_type
                INTO legacy_type
                FROM information_schema.columns
                WHERE table_schema = $schema
                  AND table_name = $tableName
                  AND column_name = $columnName;

                IF legacy_type IN ('text', 'character varying', 'character') THEN
                    EXECUTE format(
                        'ALTER TABLE %I.%I ALTER COLUMN %I TYPE uuid USING BTRIM(%I::text)::uuid',
                        $schema,
                        $tableName,
                        $columnName,
                        $columnName
                    );
                END IF;
            END;
            ${'$'}migration${'$'};
            """.trimIndent()
        )
    }
}
