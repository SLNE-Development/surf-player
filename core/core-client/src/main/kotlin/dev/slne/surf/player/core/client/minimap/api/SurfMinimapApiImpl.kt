package dev.slne.surf.player.core.client.minimap.api

import com.google.auto.service.AutoService
import dev.slne.surf.player.api.minimap.SurfMinimapApi
import dev.slne.surf.player.core.client.minimap.config.CompanionConfigs
import dev.slne.surf.player.core.client.minimap.platform.MinimapPlatform
import dev.slne.surf.player.core.client.minimap.world.LevelIds
import dev.slne.surf.player.core.client.minimap.xaero.XaeroSyncService
import net.kyori.adventure.key.Key
import net.kyori.adventure.util.Services
import java.util.*

@AutoService(SurfMinimapApi::class)
class SurfMinimapApiImpl : SurfMinimapApi, Services.Fallback {
    override fun levelId(worldKey: Key): Int? =
        MinimapPlatform.worldIdentity(worldKey)?.let { identity ->
            LevelIds.resolve(worldKey, identity, CompanionConfigs.current.config)
        }

    override fun minimapLevelId(playerUuid: UUID): Int? = XaeroSyncService.lastSentLevelId(playerUuid)
}
