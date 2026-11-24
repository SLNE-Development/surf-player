package dev.slne.surf.player.core.client.tab.hook

import dev.slne.surf.player.api.playtime.surfPlaytimeApi
import java.util.*

object SurfPlaytimeHook {
    fun isAfk(playerUuid: UUID) =
        surfPlaytimeApi.isPlayerAfk(playerUuid)
}
