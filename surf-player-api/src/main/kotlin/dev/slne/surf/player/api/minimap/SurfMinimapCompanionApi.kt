package dev.slne.surf.player.api.minimap

import dev.slne.surf.api.core.util.requiredService
import net.kyori.adventure.key.Key
import java.util.*

private val api = requiredService<SurfMinimapCompanionApi>()

interface SurfMinimapCompanionApi {

    /**
     * Returns the level ID of the world with [worldKey], or `null` if this server has no such world.
     */
    fun levelId(worldKey: Key): Int?

    /**
     * Returns the level ID last sent to the player with [playerUuid], or `null` if they are offline.
     */
    fun levelId(playerUuid: UUID): Int?

    companion object : SurfMinimapCompanionApi by api
}
