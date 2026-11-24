package dev.slne.surf.player.paper.nametag.platform

import com.github.shynixn.mccoroutine.folia.launch
import com.google.auto.service.AutoService
import dev.slne.surf.api.paper.inventory.framework.view.util.shift as glyphShift
import dev.slne.surf.player.core.client.nametag.platform.NametagPlatform
import dev.slne.surf.player.paper.nametag.PaperNametagFeature
import dev.slne.surf.player.paper.plugin
import kotlinx.coroutines.CoroutineScope
import org.bukkit.Bukkit
import java.util.*

@AutoService(NametagPlatform::class)
class PaperNametagPlatform : NametagPlatform {
    override val dataPath get() = PaperNametagFeature.dataPath

    override fun onlinePlayers() = Bukkit.getOnlinePlayers().map { PaperNametagPlayer(it) }

    override fun player(playerUuid: UUID) =
        Bukkit.getPlayer(playerUuid)?.let { PaperNametagPlayer(it) }

    override fun shift(amount: Int) = glyphShift(amount)

    override fun launch(block: suspend CoroutineScope.() -> Unit) = plugin.launch(block = block)
}
