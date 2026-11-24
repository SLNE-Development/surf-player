package dev.slne.surf.player.velocity

import java.nio.file.Path
import kotlin.io.path.createDirectories

/**
 * One feature (clan, connection) bundled into [PlayerVelocityPlugin].
 *
 * The plugin forwards its lifecycle to every feature on proxy initialization: first [onLoad] for
 * all features (before Redis is connected), then [onEnable] for all features, both in registration
 * order; [onDisable] on proxy shutdown in reverse order.
 */
interface VelocityPlayerFeature {
    /**
     * Short identifier of the feature; also the name of its subfolder in the plugin's data
     * directory.
     */
    val id: String

    /**
     * The data directory of this feature: `plugins/<plugin id>/<feature id>`.
     */
    val dataDirectory: Path
        get() = plugin.dataPath.resolve(id).createDirectories()

    suspend fun onLoad() = Unit

    suspend fun onEnable() = Unit

    suspend fun onDisable() = Unit
}
