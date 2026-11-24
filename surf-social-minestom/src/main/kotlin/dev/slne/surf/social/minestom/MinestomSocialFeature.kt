package dev.slne.surf.social.minestom

import net.minestom.server.entity.Player
import java.nio.file.Path
import kotlin.io.path.createDirectories

/**
 * One feature (chat, friends, clan, tab, ...) bundled into [SocialMinestomPlugin].
 *
 * The plugin forwards its own lifecycle and the players joining and leaving to every feature:
 * [onLoad], [onEnable], [onPlayerJoin] and [onPlayerQuit] in the order the features are
 * registered, [onDisable] in reverse order.
 */
interface MinestomSocialFeature {
    /**
     * Short identifier of the feature; also the name of its subfolder in the plugin's data
     * directory.
     */
    val id: String

    /**
     * The data directory of this feature: `plugins/<plugin id>/<feature id>`. Each feature gets its
     * own subfolder since several of them use the same file names (e.g. `config.yml`).
     */
    val dataDirectory: Path
        get() = SocialMinestomPlugin.dataDirectory.resolve(id).createDirectories()

    suspend fun onLoad() = Unit

    suspend fun onEnable() = Unit

    suspend fun onDisable() = Unit

    /**
     * Called when [player] has spawned on this server for the first time.
     */
    fun onPlayerJoin(player: Player) = Unit

    /**
     * Called when [player] disconnects from this server.
     */
    fun onPlayerQuit(player: Player) = Unit
}
