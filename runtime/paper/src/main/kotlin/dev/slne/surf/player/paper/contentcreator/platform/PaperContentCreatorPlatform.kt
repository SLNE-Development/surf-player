package dev.slne.surf.player.paper.contentcreator.platform

import com.github.shynixn.mccoroutine.folia.scope
import com.google.auto.service.AutoService
import dev.slne.surf.api.core.util.mutableObjectSetOf
import dev.slne.surf.player.core.client.contentcreator.platform.ContentCreatorPlatform
import dev.slne.surf.player.paper.contentcreator.PaperContentCreatorFeature
import dev.slne.surf.player.paper.plugin
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.coroutines.CoroutineScope
import org.bukkit.Bukkit
import java.nio.file.Path
import java.util.*

@AutoService(ContentCreatorPlatform::class)
class PaperContentCreatorPlatform : ContentCreatorPlatform {
    override val dataPath: Path
        get() = PaperContentCreatorFeature.dataPath

    override val pluginScope: CoroutineScope
        get() = plugin.scope

    override fun onlinePlayerUuids(): ObjectSet<UUID> =
        Bukkit.getOnlinePlayers().mapTo(mutableObjectSetOf()) { it.uniqueId }
}
