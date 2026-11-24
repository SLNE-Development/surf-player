package dev.slne.surf.player.velocity.settings

import com.github.shynixn.mccoroutine.velocity.launch
import com.google.auto.service.AutoService
import dev.slne.surf.player.core.client.settings.platform.SettingsPlatform
import dev.slne.surf.player.velocity.plugin
import kotlinx.coroutines.CoroutineScope
import java.util.*

@AutoService(SettingsPlatform::class)
class VelocitySettingsPlatform : SettingsPlatform {

    /** The proxy has no inventories, so there is no settings menu to open. */
    override fun openSettingsGui(playerUuid: UUID) = Unit

    override fun launchAsync(block: suspend CoroutineScope.() -> Unit) {
        plugin.container.launch {
            block()
        }
    }
}
