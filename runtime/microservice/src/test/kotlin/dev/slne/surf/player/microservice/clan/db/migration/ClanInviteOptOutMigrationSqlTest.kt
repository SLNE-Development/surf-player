package dev.slne.surf.player.microservice.clan.db.migration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ClanInviteOptOutMigrationSqlTest {
    private val sql = ClanInviteOptOutMigrationSql(
        clanSchema = "clan-live",
        settingsSchema = "settings-live",
    )

    private fun String.normalized() = replace(Regex("\\s+"), " ").trim()

    @Test
    fun `identifiers and literals are quoted`() {
        assertEquals("\"a\"\"b\".\"t\"", ClanInviteOptOutMigrationSql.qualified("a\"b", "t"))
        assertEquals("'it''s'", ClanInviteOptOutMigrationSql.literal("it's"))
    }

    @Test
    fun `column check uses the configured clan schema`() {
        val query = sql.legacyColumnExists.normalized()
        assertTrue("table_schema = 'clan-live'" in query, query)
        assertTrue("table_name = 'clan_players'" in query, query)
        assertTrue("column_name = 'accepts_clan_invites'" in query, query)
    }

    @Test
    fun `setting is registered only if missing, with its default`() {
        val query = sql.ensureSetting.normalized()
        assertTrue(query.startsWith("INSERT INTO \"settings-live\".\"settings_settings\" (name, default_value)"), query)
        assertTrue("SELECT 'clan:invites', 'true'" in query, query)
        assertTrue("WHERE NOT EXISTS (SELECT 1 FROM \"settings-live\".\"settings_settings\" WHERE name = 'clan:invites')" in query, query)
    }

    @Test
    fun `only opted-out players without a setting value are migrated`() {
        val query = sql.migrate.normalized()
        assertTrue("INSERT INTO \"settings-live\".\"settings_entries\" (setting_name, player_uuid, value)" in query, query)
        assertTrue("SELECT 'clan:invites', cp.uuid, 'false'" in query, query)
        assertTrue("FROM \"clan-live\".\"clan_players\" cp" in query, query)
        assertTrue("WHERE cp.accepts_clan_invites = false" in query, query)
        assertTrue(
            "NOT EXISTS ( SELECT 1 FROM \"settings-live\".\"settings_entries\" se " +
                "WHERE se.setting_name = 'clan:invites' AND se.player_uuid = cp.uuid )" in query,
            query,
        )
        assertTrue(query.endsWith("SELECT count(*) FROM migrated"), query)
    }
}
