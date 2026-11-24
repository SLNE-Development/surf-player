package dev.slne.surf.player.paper.tab.platform

import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.player.core.client.tab.platform.TabPlatform
import dev.slne.surf.player.paper.plugin
import dev.slne.surf.player.paper.tab.PaperTabFeature
import dev.slne.surf.player.paper.vanish.service.VanishService
import kotlinx.coroutines.CoroutineScope
import org.bukkit.Bukkit
import java.util.*

class PaperTabPlatform : TabPlatform {
    override val dataPath get() = PaperTabFeature.dataPath

    override fun onlinePlayers() = Bukkit.getOnlinePlayers().map { PaperTabPlayer(it) }

    override fun onlinePlayerCount() = Bukkit.getOnlinePlayers().size

    override fun player(playerUuid: UUID) = Bukkit.getPlayer(playerUuid)?.let { PaperTabPlayer(it) }

    override fun maxPlayerCount() = Bukkit.getMaxPlayers()

    override fun isVanished(playerUuid: UUID) = VanishService.isVanished(playerUuid)

    override fun launch(block: suspend CoroutineScope.() -> Unit) = plugin.launch(block = block)
}
