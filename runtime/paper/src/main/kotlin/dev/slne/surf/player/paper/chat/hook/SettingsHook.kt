package dev.slne.surf.player.paper.chat.hook

import dev.slne.surf.player.api.settings.SurfSettingsApi
import dev.slne.surf.player.api.settings.setting.AdvancementMessageVisibility
import dev.slne.surf.player.api.settings.setting.SettingKeys
import java.util.*

object SettingsHook {
    /** Which advancement messages the player [playerUuid] wants to see in chat. */
    fun advancementMessageVisibility(playerUuid: UUID): AdvancementMessageVisibility =
        SurfSettingsApi.getSettingValue(playerUuid, SettingKeys.CHAT_ADVANCEMENT_MESSAGES)
}
