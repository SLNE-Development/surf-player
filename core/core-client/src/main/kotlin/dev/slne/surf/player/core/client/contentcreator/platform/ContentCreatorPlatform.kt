package dev.slne.surf.player.core.client.contentcreator.platform

import dev.slne.surf.api.core.util.requiredService
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.coroutines.CoroutineScope
import java.nio.file.Path
import java.util.*

private val platform = requiredService<ContentCreatorPlatform>()

/**
 * Gives platform-neutral code access to the few things only the running platform can answer.
 *
 * Not to be confused with [dev.slne.surf.player.api.contentcreator.ContentCreatorPlatform], a
 * streaming platform of a content creator.
 */
interface ContentCreatorPlatform {

    /**
     * The directory this plugin keeps its files in.
     */
    val dataPath: Path

    val pluginScope: CoroutineScope

    /**
     * The minecraft uuids of all players currently connected to this server.
     */
    fun onlinePlayerUuids(): ObjectSet<UUID>

    companion object : ContentCreatorPlatform by platform
}
