package dev.slne.surf.player.paper.settings

import com.github.shynixn.mccoroutine.folia.launch
import com.google.auto.service.AutoService
import dev.slne.surf.player.core.client.settings.platform.SettingsPlatform
import dev.slne.surf.player.paper.plugin
import dev.slne.surf.player.paper.settings.menu.openSettingsMenu
import kotlinx.coroutines.CoroutineScope
import net.kyori.adventure.util.Services
import org.bukkit.Bukkit
import java.util.*

@AutoService(SettingsPlatform::class)
class PaperSettingsPlatform : SettingsPlatform, Services.Fallback {
    override fun openSettingsGui(playerUuid: UUID) {
        Bukkit.getPlayer(playerUuid)?.openSettingsMenu()
    }

    override fun launchAsync(block: suspend CoroutineScope.() -> Unit) {
        plugin.launch { block() }
    }
}
