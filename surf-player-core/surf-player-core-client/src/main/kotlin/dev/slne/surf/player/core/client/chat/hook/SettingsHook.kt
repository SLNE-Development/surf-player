package dev.slne.surf.player.core.client.chat.hook

import dev.slne.surf.player.api.settings.SurfSettingsApi
import dev.slne.surf.player.api.settings.setting.SettingKeys
import java.util.*

object SettingsHook {
    fun hasDirectMessagesEnabled(playerUuid: UUID): Boolean =
        SurfSettingsApi.getSettingValue(playerUuid, SettingKeys.DIRECT_MESSAGES)

    fun hasChatPingsEnabled(playerUuid: UUID): Boolean =
        SurfSettingsApi.getSettingValue(playerUuid, SettingKeys.CHAT_PINGS)

    fun hasConnectionMessagesEnabled(playerUuid: UUID): Boolean =
        SurfSettingsApi.getSettingValue(playerUuid, SettingKeys.CONNECTION_MESSAGES)
}
