package dev.slne.surf.player.core.client.profile.api

import com.google.auto.service.AutoService
import dev.slne.surf.player.api.profile.SurfProfileApi
import dev.slne.surf.player.core.client.profile.platform.ProfilePlatform
import net.kyori.adventure.util.Services
import java.util.*

@AutoService(SurfProfileApi::class)
class SurfProfileApiImpl : SurfProfileApi, Services.Fallback {
    override fun openOwnProfileMenu(playerUuid: UUID) {
        ProfilePlatform.openOwnProfileMenu(playerUuid)
    }
}
