package dev.slne.surf.social.core.client.tab.hook

import dev.slne.surf.social.api.playtime.surfPlaytimeApi
import java.util.*

object SurfPlaytimeHook {
    fun isAfk(playerUuid: UUID) =
        surfPlaytimeApi.isPlayerAfk(playerUuid)
}
