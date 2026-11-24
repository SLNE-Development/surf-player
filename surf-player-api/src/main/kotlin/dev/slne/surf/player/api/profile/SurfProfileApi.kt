package dev.slne.surf.player.api.profile

import dev.slne.surf.api.core.util.requiredService
import java.util.*

val surfProfileApi = requiredService<SurfProfileApi>()

interface SurfProfileApi {
    fun openOwnProfileMenu(playerUuid: UUID)
}