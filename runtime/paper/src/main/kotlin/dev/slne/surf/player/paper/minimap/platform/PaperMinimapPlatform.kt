package dev.slne.surf.player.paper.minimap.platform

import com.google.auto.service.AutoService
import dev.slne.surf.player.core.client.minimap.platform.MinimapPlatform
import dev.slne.surf.player.paper.minimap.PaperMinimapFeature
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit

@AutoService(MinimapPlatform::class)
class PaperMinimapPlatform : MinimapPlatform {
    override val dataPath get() = PaperMinimapFeature.dataPath

    override fun worldIdentity(worldKey: Key) = Bukkit.getWorld(worldKey)?.uid?.toString()

    override val tracksPlayers = true
}
