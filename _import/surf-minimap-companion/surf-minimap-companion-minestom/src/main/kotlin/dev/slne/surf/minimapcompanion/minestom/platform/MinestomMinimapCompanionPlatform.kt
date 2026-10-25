package dev.slne.surf.minimapcompanion.minestom.platform

import com.google.auto.service.AutoService
import dev.slne.surf.minimapcompanion.core.platform.MinimapCompanionPlatform
import dev.slne.surf.minimapcompanion.minestom.MinimapCompanionMinestomPlugin
import net.kyori.adventure.key.Key
import net.minestom.server.MinecraftServer

@AutoService(MinimapCompanionPlatform::class)
class MinestomMinimapCompanionPlatform : MinimapCompanionPlatform {
    override val dataPath get() = MinimapCompanionMinestomPlugin.dataDirectory

    override fun worldIdentity(worldKey: Key) = MinecraftServer.getInstanceManager().instances
        .firstOrNull { it.dimensionName == worldKey.asString() }
        ?.dimensionName
}
