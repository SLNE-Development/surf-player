package dev.slne.surf.social.core.client.profile.integration

import dev.slne.surf.social.api.settings.SurfSettingsApi
import java.util.*

object SettingsIntegration {
    fun openMenu(playerUuid: UUID) {
        SurfSettingsApi.openSettingsGui(playerUuid)
    }
}
