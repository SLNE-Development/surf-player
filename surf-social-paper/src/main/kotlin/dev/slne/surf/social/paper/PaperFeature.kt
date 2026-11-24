package dev.slne.surf.social.paper

import java.nio.file.Path
import kotlin.io.path.createDirectories

/**
 * A feature (former standalone plugin) bundled into [SocialPaperPlugin].
 *
 * Lifecycle methods are called by [SocialPaperPlugin] in feature order on load/enable
 * and in reverse order on disable.
 */
interface PaperFeature {
    /** Human-readable feature name, also used as the feature's data subfolder. */
    val name: String

    /** Feature-specific data folder inside the shared plugin data folder. */
    val dataPath: Path
        get() = plugin.dataPath.resolve(name).createDirectories()

    suspend fun onLoad() {}
    suspend fun onEnable() {}
    suspend fun onDisable() {}
}
