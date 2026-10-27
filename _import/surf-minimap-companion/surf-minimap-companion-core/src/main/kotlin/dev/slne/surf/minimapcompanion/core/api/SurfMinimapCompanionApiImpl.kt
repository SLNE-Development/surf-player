package dev.slne.surf.minimapcompanion.core.api

import com.google.auto.service.AutoService
import dev.slne.surf.minimapcompanion.api.SurfMinimapCompanionApi
import dev.slne.surf.minimapcompanion.core.config.CompanionConfigs
import dev.slne.surf.minimapcompanion.core.world.LevelIds
import dev.slne.surf.minimapcompanion.core.platform.CompanionPlatform
import dev.slne.surf.minimapcompanion.core.xaero.XaeroSyncService
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
