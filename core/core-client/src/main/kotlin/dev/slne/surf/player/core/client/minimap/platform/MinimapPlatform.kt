package dev.slne.surf.player.core.client.minimap.platform

import dev.slne.surf.api.core.util.requiredService
import net.kyori.adventure.key.Key
import java.nio.file.Path

private val platform = requiredService<MinimapPlatform>()

/**
 * The platform specific calls the shared companion logic depends on.
 */
interface MinimapPlatform {

    /**
     * The directory this plugin keeps its files in.
     */
    val dataPath: Path

    /**
     * Returns the identity the level ID of the world with [worldKey] is computed from,
     * or `null` if this server has no such world.
     */
    fun worldIdentity(worldKey: Key): String?

    /**
     * Whether this platform runs the player tracker. Only then may a map mod rely on the server for players.
     */
    val tracksPlayers: Boolean

    companion object : MinimapPlatform by platform
}
