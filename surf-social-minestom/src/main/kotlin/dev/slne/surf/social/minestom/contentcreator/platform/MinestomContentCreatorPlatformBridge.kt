package dev.slne.surf.social.minestom.contentcreator.platform

import com.google.auto.service.AutoService
import dev.slne.surf.api.core.util.mutableObjectSetOf
import dev.slne.surf.api.minestom.coroutine.minestomAsyncScope
import dev.slne.surf.api.minestom.extension.ConnectionManager
import dev.slne.surf.social.core.client.contentcreator.platform.ContentCreatorPlatformBridge
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.coroutines.CoroutineScope
import java.util.*

@AutoService(ContentCreatorPlatformBridge::class)
class MinestomContentCreatorPlatformBridge : ContentCreatorPlatformBridge {
    override val pluginScope: CoroutineScope
        get() = minestomAsyncScope

    override fun onlinePlayerUuids(): ObjectSet<UUID> =
        ConnectionManager.onlinePlayers.mapTo(mutableObjectSetOf()) { it.uuid }
}
