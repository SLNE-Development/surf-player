package dev.slne.surf.player.paper.clan.hook

import dev.slne.surf.player.placeholder.expansion.clan.ClanDataCache
import dev.slne.surf.player.placeholder.expansion.clan.ClanExpansionProvider
import io.github.miniplaceholders.api.Expansion
import io.papermc.paper.event.connection.configuration.AsyncPlayerConnectionConfigureEvent
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import kotlin.time.Duration.Companion.seconds

/**
 * The MiniPlaceholders clan expansion (`<clan_name>`, `<clan_tag>`, `<clan_tag_raw>`) on Paper, the
 * same one Velocity loads from its expansion folder. MiniPlaceholders is a required dependency of
 * surf-player-paper, so the plugin registers the expansion itself instead of needing a jar in
 * `plugins/MiniPlaceholders/expansions`.
 */
object PaperClanPlaceholders : Listener {
    private var expansion: Expansion? = null

    fun registerExpansion() {
        if (expansion != null) return
        expansion = ClanExpansionProvider.provideExpansion().also { it.register() }
    }

    fun unregisterExpansion() {
        expansion?.takeIf { it.registered() }?.unregister()
        expansion = null
    }

    /**
     * Loads the connecting player's clan before they join, so the placeholders already show it in the
     * chat MOTD sent on join; the expansion itself never waits for a lookup.
     */
    @EventHandler(priority = EventPriority.LOW)
    fun onConfigure(event: AsyncPlayerConnectionConfigureEvent) {
        val uuid = event.connection.profile.id ?: return

        runBlocking {
            withTimeoutOrNull(2.seconds) { ClanDataCache.preload(uuid) }
        }
    }
}
