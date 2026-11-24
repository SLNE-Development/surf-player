package dev.slne.surf.social.velocity.settings

import com.github.shynixn.mccoroutine.velocity.launch
import com.google.auto.service.AutoService
import dev.slne.surf.social.core.client.settings.platform.SettingsPlatform
import kotlinx.coroutines.CoroutineScope
import java.util.UUID

@AutoService(SettingsPlatform::class)
class VelocitySettingsPlatform : SettingsPlatform {

    override fun openSettingsGui(playerUuid: UUID) {
        // Not implemented on velocity. Fail silently
    }

    override fun launchAsync(block: suspend CoroutineScope.() -> Unit) {
        plugin.container.launch {
            block()
        }
    }
}