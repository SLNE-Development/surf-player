package dev.slne.surf.player.core.client.chat.platform

import dev.slne.surf.player.core.client.settings.service.SettingsService
import java.util.*

/**
 * Whether [targetUuid], who is on this server, may be shown a signed message that requires the
 * boolean setting [requiredSetting]; always `true` without one. Checked on the server hosting the
 * target, the only one that has their settings cached.
 */
fun receivesSignedMessage(targetUuid: UUID, requiredSetting: String?): Boolean {
    if (requiredSetting == null) return true

    return SettingsService.getSettingValueOrDefault(targetUuid, requiredSetting)
        ?.toBooleanStrictOrNull()
        ?: true
}
