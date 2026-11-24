package dev.slne.surf.player.paper

import dev.slne.surf.player.paper.command.PaperAdminAction
import java.nio.file.Path
import kotlin.io.path.createDirectories

/**
 * A feature (former standalone plugin) bundled into [PlayerPaperPlugin].
 *
 * Lifecycle methods are called by [PlayerPaperPlugin] in feature order on load/enable
 * and in reverse order on disable.
 */
interface PaperPlayerFeature {
    /** Human-readable feature name, also used as the feature's data subfolder. */
    val name: String

    /** Feature-specific data folder inside the shared plugin data folder. */
    val dataPath: Path
        get() = plugin.dataPath.resolve(name).createDirectories()

    suspend fun onLoad() {}
    suspend fun onEnable() {}
    suspend fun onDisable() {}

    /**
     * The actions this feature adds below `/surfplayer <name>`. Read once, after every feature was
     * enabled.
     */
    fun adminActions(): List<PaperAdminAction> = emptyList()
}
