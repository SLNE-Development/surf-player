package dev.slne.surf.player.api.minimap

import dev.slne.surf.api.core.util.requiredService
import net.kyori.adventure.key.Key
import java.util.*

private val instance = requiredService<SurfMinimapApi>()

interface SurfMinimapApi {

    /**
     * Returns the level ID of the world with [worldKey], or `null` if this server has no such world.
     */
    fun levelId(worldKey: Key): Int?

    /**
     * Returns the level ID last sent to the player with [playerUuid], or `null` if they are offline.
     */
    fun minimapLevelId(playerUuid: UUID): Int?

    companion object : SurfMinimapApi by instance {
        val INSTANCE: SurfMinimapApi get() = instance
    }
}
