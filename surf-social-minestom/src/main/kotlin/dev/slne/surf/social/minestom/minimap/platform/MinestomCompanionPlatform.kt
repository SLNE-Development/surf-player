package dev.slne.surf.social.minestom.minimap.platform

import com.google.auto.service.AutoService
import dev.slne.surf.social.core.client.minimap.platform.CompanionPlatform
import dev.slne.surf.social.minestom.minimap.MinimapCompanionMinestomPlugin
import net.kyori.adventure.key.Key
import net.minestom.server.MinecraftServer

@AutoService(CompanionPlatform::class)
class MinestomCompanionPlatform : CompanionPlatform {
    override val dataPath get() = MinimapCompanionMinestomPlugin.dataDirectory

    override fun worldIdentity(worldKey: Key) = MinecraftServer.getInstanceManager().instances
        .firstOrNull { it.dimensionName == worldKey.asString() }
        ?.dimensionName

    override val tracksPlayers = false
}
