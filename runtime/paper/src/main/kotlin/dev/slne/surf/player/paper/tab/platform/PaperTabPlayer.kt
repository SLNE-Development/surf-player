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

    /**
     * The player's display name, as surf-tab used it, so display names set by other plugins
     * (including a prefix they add) show up in the tab list. No LuckPerms prefix is added here.
     */
    override fun baseName(): Component = player.displayName()

    override suspend fun baseNameSnapshot(): Component =
        withContext(plugin.entityDispatcher(player)) { baseName() }

    override suspend fun showTabEntry(name: Component, order: Int) {
        withContext(plugin.entityDispatcher(player)) {
            player.playerListName(name)
            // Paper rejects negative orders; surf-tab passed negative LuckPerms weights through,
            // which threw here. Such entries are listed with order 0 instead.
            player.playerListOrder = order.coerceAtLeast(0)
        }
    }
}
