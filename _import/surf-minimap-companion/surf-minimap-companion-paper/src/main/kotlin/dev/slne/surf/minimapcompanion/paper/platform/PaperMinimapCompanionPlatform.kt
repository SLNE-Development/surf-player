package dev.slne.surf.minimapcompanion.paper.platform

import com.google.auto.service.AutoService
import dev.slne.surf.minimapcompanion.core.platform.MinimapCompanionPlatform
import dev.slne.surf.minimapcompanion.paper.plugin
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit

@AutoService(MinimapCompanionPlatform::class)
class PaperMinimapCompanionPlatform : MinimapCompanionPlatform {
    override val dataPath get() = plugin.dataPath

    override fun worldIdentity(worldKey: Key) = Bukkit.getWorld(worldKey)?.uid?.toString()
}
