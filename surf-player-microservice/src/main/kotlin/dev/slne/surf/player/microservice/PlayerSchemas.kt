package dev.slne.surf.player.microservice

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.Schema
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.Table
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.SchemaUtils

/**
 * PostgreSQL schema names of the merged social features.
 *
 * chat, clan, friends and shrieker used to be separate microservices sharing one PostgreSQL
 * database, each with its own schema selected through its own `SURF_DATABASE_SCHEMA`. The merged
 * [PlayerMicroservice] has only one `DatabaseApi` and therefore one `SURF_DATABASE_SCHEMA`, so every
 * feature table is schema-qualified with the value from this object instead (for example
 * `"surf-chat.chat_histories"`), keeping each table in the schema it lived in before.
 *
 * **These values MUST match the `SURF_DATABASE_SCHEMA` values of the old, separate deployments.**
 * If they do not, the feature starts against an empty schema: its tables are created fresh there
 * and the existing data is silently ignored. Override a default with the matching environment
 * variable:
 *
 * | Feature  | Default         | Environment variable         |
 * |----------|-----------------|------------------------------|
 * | chat     | `surf-chat`     | `SURF_PLAYER_SCHEMA_CHAT`     |
 * | clan     | `surf-clan`     | `SURF_PLAYER_SCHEMA_CLAN`     |
 * | friends  | `surf-friends`  | `SURF_PLAYER_SCHEMA_FRIENDS`  |
 * | shrieker | `surf-shrieker` | `SURF_PLAYER_SCHEMA_SHRIEKER` |
 * | settings | `surf-settings` | `SURF_PLAYER_SCHEMA_SETTINGS` |
 * | playtime | `surf-playtime` | `SURF_PLAYER_SCHEMA_PLAYTIME` |
 *
 * The variables are read once, when this object initializes, which happens before any table
 * object initializes because the table names reference it. Changing them requires a restart.
 *
 * Names must be lowercase and must not contain `.` or `"`: Exposed lower-cases the whole
 * schema-qualified table name on PostgreSQL and splits it at the last `.`, so anything else would
 * address a different schema than the one configured.
 */
object PlayerSchemas {
    val chat: String = schema("SURF_PLAYER_SCHEMA_CHAT", "surf-chat")
    val clan: String = schema("SURF_PLAYER_SCHEMA_CLAN", "surf-clan")
    val friends: String = schema("SURF_PLAYER_SCHEMA_FRIENDS", "surf-friends")
    val shrieker: String = schema("SURF_PLAYER_SCHEMA_SHRIEKER", "surf-shrieker")
    val settings: String = schema("SURF_PLAYER_SCHEMA_SETTINGS", "surf-settings")
    val playtime: String = schema("SURF_PLAYER_SCHEMA_PLAYTIME", "surf-playtime")

    private fun schema(envVar: String, default: String): String {
        val name = System.getenv(envVar)?.trim()?.takeIf { it.isNotEmpty() } ?: default
        require(name == name.lowercase()) { "$envVar must be lowercase, got '$name'" }
        require('.' !in name && '"' !in name) { "$envVar must not contain '.' or '\"', got '$name'" }
        return name
    }
}

/**
 * Creates [schema] if it does not exist yet, then the missing [tables] in it.
 * Must be called inside a transaction.
 */
suspend fun createSchemaWithTables(schema: String, vararg tables: Table) {
    SchemaUtils.createSchema(Schema(schema))
    SchemaUtils.create(*tables)
}
