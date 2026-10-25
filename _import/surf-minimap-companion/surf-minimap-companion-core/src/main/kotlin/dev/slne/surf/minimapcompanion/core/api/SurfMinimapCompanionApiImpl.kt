package dev.slne.surf.minimapcompanion.core.api

import com.google.auto.service.AutoService
import dev.slne.surf.minimapcompanion.api.SurfMinimapCompanionApi
import dev.slne.surf.minimapcompanion.core.config.CompanionConfig
import dev.slne.surf.minimapcompanion.core.id.LevelIds
import dev.slne.surf.minimapcompanion.core.platform.MinimapCompanionPlatform
import dev.slne.surf.minimapcompanion.core.service.XaeroSyncService
import net.kyori.adventure.key.Key
import net.kyori.adventure.util.Services
import java.util.*

@AutoService(SurfMinimapCompanionApi::class)
class SurfMinimapCompanionApiImpl : SurfMinimapCompanionApi, Services.Fallback {
    override fun levelId(worldKey: Key): Int? =
        MinimapCompanionPlatform.worldIdentity(worldKey)?.let { identity ->
            LevelIds.resolve(worldKey, identity, CompanionConfig.getConfig())
        }

    override fun levelId(playerUuid: UUID): Int? = XaeroSyncService.lastSentLevelId(playerUuid)
}
