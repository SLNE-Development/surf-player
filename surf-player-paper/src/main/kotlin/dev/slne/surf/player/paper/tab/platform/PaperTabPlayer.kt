package dev.slne.surf.player.paper.tab.platform

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import dev.slne.surf.player.core.client.tab.platform.TabPlayer
import dev.slne.surf.player.paper.plugin
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

class PaperTabPlayer(private val player: Player) : TabPlayer {
    override val uuid get() = player.uniqueId
    override val audience get() = player

    override fun baseName(): Component = player.displayName()

    override suspend fun baseNameSnapshot(): Component =
        withContext(plugin.entityDispatcher(player)) { baseName() }

    override suspend fun showTabEntry(name: Component, order: Int) {
        withContext(plugin.entityDispatcher(player)) {
            player.playerListName(name)
            player.playerListOrder = order
        }
    }
}
