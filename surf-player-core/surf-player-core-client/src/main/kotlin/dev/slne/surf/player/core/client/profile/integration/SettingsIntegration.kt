package dev.slne.surf.player.core.client.profile.integration

import dev.slne.surf.player.api.settings.SurfSettingsApi
import java.util.*

object SettingsIntegration {
    fun openMenu(playerUuid: UUID) {
        SurfSettingsApi.openSettingsGui(playerUuid)
    }
}
