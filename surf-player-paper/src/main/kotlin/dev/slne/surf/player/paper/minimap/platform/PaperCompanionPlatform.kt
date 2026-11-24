package dev.slne.surf.player.paper.minimap.platform

import com.google.auto.service.AutoService
import dev.slne.surf.player.core.client.minimap.platform.CompanionPlatform
import dev.slne.surf.player.paper.minimap.plugin
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit

@AutoService(CompanionPlatform::class)
class PaperCompanionPlatform : CompanionPlatform {
    override val dataPath get() = plugin.dataPath

    override fun worldIdentity(worldKey: Key) = Bukkit.getWorld(worldKey)?.uid?.toString()

    override val tracksPlayers = true
}
