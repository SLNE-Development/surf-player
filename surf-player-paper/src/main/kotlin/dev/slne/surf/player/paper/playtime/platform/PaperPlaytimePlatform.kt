package dev.slne.surf.player.paper.playtime.platform

import com.github.shynixn.mccoroutine.folia.launch
import com.google.auto.service.AutoService
import dev.slne.surf.api.paper.extensions.pluginManager
import dev.slne.surf.player.core.client.playtime.platform.PlaytimePlatform
import dev.slne.surf.player.paper.plugin
import kotlinx.coroutines.CoroutineScope
import org.bukkit.Bukkit
import java.util.*

@AutoService(PlaytimePlatform::class)
class PaperPlaytimePlatform : PlaytimePlatform {
    override val transactionAvailable get() = pluginManager.isPluginEnabled("surf-transaction-paper")

    override fun onlinePlayer(playerUuid: UUID) = Bukkit.getPlayer(playerUuid)

    override fun launchAsync(block: suspend CoroutineScope.() -> Unit) = plugin.launch(block = block)
}
