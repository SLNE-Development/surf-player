package dev.slne.surf.player.velocity

import dev.slne.surf.player.velocity.command.VelocityAdminAction
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

    /**
     * Creates and loads every config file of the feature first (so they exist right after
     * startup, not only after a reload), then sets up whatever must exist before enabling.
     */
    suspend fun onLoad() = Unit

    suspend fun onEnable() = Unit

    suspend fun onDisable() = Unit

    /**
     * The actions this feature adds below `/surfplayervelocity <id>`. Read once, after every
     * feature was enabled.
     */
    fun adminActions(): List<VelocityAdminAction> = emptyList()
}
