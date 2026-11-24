package dev.slne.surf.social.paper.contentcreator.platform

import com.github.shynixn.mccoroutine.folia.scope
import com.google.auto.service.AutoService
import dev.slne.surf.api.core.util.mutableObjectSetOf
import dev.slne.surf.social.core.client.contentcreator.platform.ContentCreatorPlatformBridge
import dev.slne.surf.social.paper.plugin
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.coroutines.CoroutineScope
import org.bukkit.Bukkit
import java.util.*

@AutoService(ContentCreatorPlatformBridge::class)
class PaperContentCreatorPlatformBridge : ContentCreatorPlatformBridge {
    override val pluginScope: CoroutineScope
        get() = plugin.scope

    override fun onlinePlayerUuids(): ObjectSet<UUID> =
        Bukkit.getOnlinePlayers().mapTo(mutableObjectSetOf()) { it.uniqueId }
}
