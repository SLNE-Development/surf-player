package dev.slne.surf.player.minestom.minimap.platform

import com.google.auto.service.AutoService
import dev.slne.surf.player.core.client.minimap.platform.MinimapPlatform
import dev.slne.surf.player.minestom.minimap.MinimapMinestomFeature
import net.kyori.adventure.key.Key
import net.minestom.server.MinecraftServer

@AutoService(MinimapPlatform::class)
class MinestomMinimapPlatform : MinimapPlatform {
    override val dataPath get() = MinimapMinestomFeature.dataDirectory

    override fun worldIdentity(worldKey: Key) = MinecraftServer.getInstanceManager().instances
        .firstOrNull { it.dimensionName == worldKey.asString() }
        ?.dimensionName

    override val tracksPlayers = false
}
