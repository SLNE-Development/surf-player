package dev.slne.surf.minimapcompanion.core.platform

import dev.slne.surf.api.core.util.requiredService
import net.kyori.adventure.key.Key
import java.nio.file.Path

private val platform = requiredService<MinimapCompanionPlatform>()

/**
 * The platform specific calls the shared companion logic depends on.
 */
interface MinimapCompanionPlatform {

    /**
     * The directory this plugin keeps its files in.
     */
    val dataPath: Path

    /**
     * Returns the identity the level ID of the world with [worldKey] is computed from,
     * or `null` if this server has no such world.
     */
    fun worldIdentity(worldKey: Key): String?

    companion object : MinimapCompanionPlatform by platform
}
