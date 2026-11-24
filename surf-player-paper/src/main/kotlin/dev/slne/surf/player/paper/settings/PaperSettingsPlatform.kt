package dev.slne.surf.social.paper.settings

import com.github.shynixn.mccoroutine.folia.launch
import com.google.auto.service.AutoService
import dev.slne.surf.api.paper.inventory.framework.open
import dev.slne.surf.social.core.client.settings.platform.SettingsPlatform
import dev.slne.surf.social.paper.settings.menu.settingsView
import kotlinx.coroutines.CoroutineScope
import net.kyori.adventure.util.Services
import org.bukkit.Bukkit
import java.util.*

@AutoService(SettingsPlatform::class)
class PaperSettingsPlatform : SettingsPlatform, Services.Fallback {
    override fun openSettingsGui(playerUuid: UUID) {
        Bukkit.getPlayer(playerUuid)?.let { player ->
            player.closeInventory()
            settingsView.open(player)
        }
    }

    override fun launchAsync(block: suspend CoroutineScope.() -> Unit) {
        plugin.launch { block() }
    }
}
