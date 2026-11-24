package dev.slne.surf.social.core.client.profile.api

import com.google.auto.service.AutoService
import dev.slne.surf.social.api.profile.SurfProfileApi
import dev.slne.surf.social.core.client.profile.platform.ProfilePlatform
import net.kyori.adventure.util.Services
import java.util.*

@AutoService(SurfProfileApi::class)
class SurfProfileApiImpl : SurfProfileApi, Services.Fallback {
    override fun openOwnProfileMenu(playerUuid: UUID) {
        ProfilePlatform.openOwnProfileMenu(playerUuid)
    }
}
