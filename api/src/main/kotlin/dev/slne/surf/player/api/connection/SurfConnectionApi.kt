package dev.slne.surf.player.api.connection

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.player.api.connection.impl.DiscordConnection
import dev.slne.surf.player.api.connection.impl.TwitchConnection
import java.util.*

private val instance = requiredService<SurfConnectionApi>()

/**
 * Entry point to the social media accounts (Discord, Twitch) players linked to their Minecraft
 * account.
 *
 * Everything about a single player is also available through [ConnectionCapability].
 */
interface SurfConnectionApi {

    /**
     * The connection of type [connectionClass] the player identified by [uuid] linked, or `null`
     * if they did not link one or the type is not supported.
     */
    suspend fun <T : SocialConnection> findConnection(uuid: UUID, connectionClass: Class<T>): T?

    /** The Discord account the player identified by [uuid] linked, or `null`. */
    suspend fun findDiscordConnection(uuid: UUID): DiscordConnection?

    /** The Twitch account the player identified by [uuid] linked, or `null`. */
    suspend fun findTwitchConnection(uuid: UUID): TwitchConnection?

    companion object : SurfConnectionApi by instance {
        val INSTANCE: SurfConnectionApi get() = instance
    }
}
