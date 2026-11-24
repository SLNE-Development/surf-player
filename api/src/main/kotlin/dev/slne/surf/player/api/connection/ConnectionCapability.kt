package dev.slne.surf.player.api.connection

import dev.slne.surf.player.api.connection.impl.DiscordConnection
import dev.slne.surf.player.api.connection.impl.TwitchConnection
import java.util.*

/** The linked social media accounts of a single player. See [SurfConnectionApi]. */
interface ConnectionCapability {
    val uuid: UUID

    /** The Discord account this player linked, or `null`. */
    suspend fun discordConnection(): DiscordConnection? = SurfConnectionApi.findDiscordConnection(uuid)

    /** The Twitch account this player linked, or `null`. */
    suspend fun twitchConnection(): TwitchConnection? = SurfConnectionApi.findTwitchConnection(uuid)

    /** The connection of type [connectionClass] this player linked, or `null`. */
    suspend fun <T : SocialConnection> socialConnection(connectionClass: Class<T>): T? =
        SurfConnectionApi.findConnection(uuid, connectionClass)
}

/** The connection of type [T] this player linked, or `null`. */
suspend inline fun <reified T : SocialConnection> ConnectionCapability.socialConnection(): T? =
    socialConnection(T::class.java)
