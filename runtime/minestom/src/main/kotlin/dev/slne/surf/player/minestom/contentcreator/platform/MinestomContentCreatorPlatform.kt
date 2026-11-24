package dev.slne.surf.player.minestom.contentcreator.platform

import com.google.auto.service.AutoService
import dev.slne.surf.api.core.util.mutableObjectSetOf
import dev.slne.surf.api.minestom.coroutine.minestomAsyncScope
import dev.slne.surf.api.minestom.extension.ConnectionManager
import dev.slne.surf.player.core.client.contentcreator.platform.ContentCreatorPlatform
import dev.slne.surf.player.minestom.contentcreator.ContentCreatorMinestomFeature
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.coroutines.CoroutineScope
import java.nio.file.Path
import java.util.*

@AutoService(ContentCreatorPlatform::class)
class MinestomContentCreatorPlatform : ContentCreatorPlatform {
    override val dataPath: Path
        get() = ContentCreatorMinestomFeature.dataDirectory

    override val pluginScope: CoroutineScope
        get() = minestomAsyncScope

    override fun onlinePlayerUuids(): ObjectSet<UUID> =
        ConnectionManager.onlinePlayers.mapTo(mutableObjectSetOf()) { it.uuid }
}
