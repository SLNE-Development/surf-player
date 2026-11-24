package dev.slne.surf.social.core.client.minimap.api

import com.google.auto.service.AutoService
import dev.slne.surf.social.api.minimap.SurfMinimapCompanionApi
import dev.slne.surf.social.core.client.minimap.config.CompanionConfigs
import dev.slne.surf.social.core.client.minimap.world.LevelIds
import dev.slne.surf.social.core.client.minimap.platform.CompanionPlatform
import dev.slne.surf.social.core.client.minimap.xaero.XaeroSyncService
import net.kyori.adventure.key.Key
import net.kyori.adventure.util.Services
import java.util.*

@AutoService(SurfMinimapCompanionApi::class)
class SurfMinimapCompanionApiImpl : SurfMinimapCompanionApi, Services.Fallback {
    override fun levelId(worldKey: Key): Int? =
        CompanionPlatform.worldIdentity(worldKey)?.let { identity ->
            LevelIds.resolve(worldKey, identity, CompanionConfigs.current.config)
        }

    override fun levelId(playerUuid: UUID): Int? = XaeroSyncService.lastSentLevelId(playerUuid)
}
