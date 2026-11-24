package dev.slne.surf.player.microservice.clan.db.migration

import dev.slne.surf.api.core.util.logger
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.Table
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.statements.StatementType
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.vendors.PostgreSQLDialect
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.vendors.currentDialect
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.TransactionManager
import dev.slne.surf.player.api.settings.setting.SettingKeys
import dev.slne.surf.player.microservice.clan.db.table.ClanPlayerTable
import dev.slne.surf.player.microservice.settings.table.SettingEntriesTable
import dev.slne.surf.player.microservice.settings.table.SettingsTable
import kotlinx.coroutines.flow.firstOrNull

private val log = logger()

/**
 * Builds the SQL of [migrateClanInviteOptOuts]. Kept free of any database access so it can be
 * unit-tested.
 *
 * @param clanSchema schema of the legacy `clan_players` table
 * @param settingsSchema schema of `settings_settings` and `settings_entries`
 */
class ClanInviteOptOutMigrationSql(
    private val clanSchema: String,
    private val clanPlayersTable: String = "clan_players",
    private val settingsSchema: String,
    private val settingsTable: String = "settings_settings",
    private val settingEntriesTable: String = "settings_entries",
    private val settingName: String = SettingKeys.CLAN_INVITES.name,
    private val settingDefault: String = SettingKeys.CLAN_INVITES.serialize(SettingKeys.CLAN_INVITES.defaultValue),
    private val optOutValue: String = SettingKeys.CLAN_INVITES.serialize(false),
) {
    private val clanPlayers = qualified(clanSchema, clanPlayersTable)
    private val settings = qualified(settingsSchema, settingsTable)
    private val entries = qualified(settingsSchema, settingEntriesTable)

    /** Counts the legacy column (0 once it was dropped). */
    val legacyColumnExists: String = """
        SELECT count(*)
        FROM information_schema.columns
        WHERE table_schema = ${literal(clanSchema)}
          AND table_name = ${literal(clanPlayersTable)}
          AND column_name = ${literal(LEGACY_COLUMN)}
    """.trimIndent()

    /**
     * Registers the setting if no server did yet; the foreign key of `settings_entries` needs it.
     * An existing setting (and its default) is left alone.
     */
    val ensureSetting: String = """
        INSERT INTO $settings (name, default_value)
        SELECT ${literal(settingName)}, ${literal(settingDefault)}
        WHERE NOT EXISTS (SELECT 1 FROM $settings WHERE name = ${literal(settingName)})
    """.trimIndent()

    /**
     * Inserts the opt-out value for every player who refused clan invites in the legacy column and
     * has no value for the setting yet, and returns the number of inserted rows. Players who
     * already chose a value keep it, so running it again inserts nothing.
     */
    val migrate: String = """
        WITH migrated AS (
            INSERT INTO $entries (setting_name, player_uuid, value)
            SELECT ${literal(settingName)}, cp.uuid, ${literal(optOutValue)}
            FROM $clanPlayers cp
            WHERE cp.$LEGACY_COLUMN = false
              AND NOT EXISTS (
                  SELECT 1
                  FROM $entries se
                  WHERE se.setting_name = ${literal(settingName)}
                    AND se.player_uuid = cp.uuid
              )
            RETURNING player_uuid
        )
        SELECT count(*) FROM migrated
    """.trimIndent()

    companion object {
        const val LEGACY_COLUMN = "accepts_clan_invites"

        /** Quotes [value] as a PostgreSQL string literal. */
        fun literal(value: String) = "'" + value.replace("'", "''") + "'"

        /** Quotes [name] as a PostgreSQL identifier. */
        fun identifier(name: String) = "\"" + name.replace("\"", "\"\"") + "\""

        fun qualified(schema: String, table: String) = identifier(schema) + "." + identifier(table)
    }
}

private fun Table.schema() = checkNotNull(schemaName) { "Table $tableName is not schema-qualified" }

/**
 * One-time migration of the clan invite opt-outs from the legacy column
 * `clan_players.accepts_clan_invites` into the setting `clan:invites`, which decides whether a
 * player accepts clan invites now.
 *
 * Idempotent: only players without a value for the setting get one, so it is safe on every
 * startup, and it does nothing once the column was dropped. Must run inside the startup
 * transaction, after the clan and settings tables exist.
 */
suspend fun migrateClanInviteOptOuts() {
    if (currentDialect !is PostgreSQLDialect) return

    val sql = ClanInviteOptOutMigrationSql(
        clanSchema = ClanPlayerTable.schema(),
        clanPlayersTable = ClanPlayerTable.nameInDatabaseCaseUnquoted(),
        settingsSchema = SettingEntriesTable.schema(),
        settingsTable = SettingsTable.nameInDatabaseCaseUnquoted(),
        settingEntriesTable = SettingEntriesTable.nameInDatabaseCaseUnquoted(),
    )
    val transaction = TransactionManager.current()

    suspend fun count(statement: String): Long =
        transaction.exec(statement, explicitStatementType = StatementType.SELECT) { row ->
            (row.get(0) as Number).toLong()
        }?.firstOrNull() ?: 0L

    if (count(sql.legacyColumnExists) == 0L) {
        log.atInfo().log(
            "Clan invite opt-out migration skipped: column %s no longer exists",
            ClanInviteOptOutMigrationSql.LEGACY_COLUMN
        )
        return
    }

    transaction.exec(sql.ensureSetting)
    val migrated = count(sql.migrate)

    log.atInfo().log("Migrated %d clan invite opt-outs to the setting %s", migrated, SettingKeys.CLAN_INVITES.name)
}
