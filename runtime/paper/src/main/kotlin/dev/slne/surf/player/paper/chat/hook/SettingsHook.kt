package dev.slne.surf.player.paper.chat.hook

import dev.slne.surf.player.api.settings.SurfSettingsApi
import dev.slne.surf.player.api.settings.setting.SettingKeys
import java.util.*

object SettingsHook {
    fun hasAdvancementsEnabled(playerUuid: UUID) =
        SurfSettingsApi.getSettingValue(playerUuid, SettingKeys.CHAT_ADVANCEMENTS)
}
