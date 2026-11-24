package dev.slne.surf.player.microservice.connection.table

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.Table

/**
 * The website's account links (`public.account`, Auth.js): one row per provider account of a website
 * user. The Minecraft row holds the player's uuid as `providerAccountId`; the Discord and Twitch rows
 * hold the numeric account id of that platform.
 */
object AccountsTable : Table("public.account") {
    /**
     * The website user's id. Only ever compared with itself, so it is kept as written: Auth.js
     * generates it, and it is not necessarily a uuid.
     */
    val userId = text("userId")

    val provider = text("provider")
    val providerAccountId = text("providerAccountId")

    override val primaryKey = PrimaryKey(provider, providerAccountId)
}
