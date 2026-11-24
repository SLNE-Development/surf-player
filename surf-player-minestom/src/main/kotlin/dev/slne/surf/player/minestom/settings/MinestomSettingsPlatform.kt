package dev.slne.surf.player.minestom.settings

import com.google.auto.service.AutoService
import dev.slne.surf.api.minestom.coroutine.minestomAsyncScope
import dev.slne.surf.api.minestom.extension.ConnectionManager
import dev.slne.surf.api.minestom.inventory.framework.open
import dev.slne.surf.player.core.client.settings.platform.SettingsPlatform
import dev.slne.surf.player.minestom.settings.menu.settingsView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.kyori.adventure.util.Services
import java.util.*

@AutoService(SettingsPlatform::class)
class MinestomSettingsPlatform : SettingsPlatform, Services.Fallback {
    override fun openSettingsGui(playerUuid: UUID) {
        val player = ConnectionManager.getOnlinePlayerByUuid(playerUuid) ?: return
        player.closeInventory()
        settingsView.open(player)
    }

    override fun launchAsync(block: suspend CoroutineScope.() -> Unit) {
        minestomAsyncScope.launch { block() }
    }
}
