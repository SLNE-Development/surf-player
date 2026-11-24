package dev.slne.surf.player.minestom

import dev.slne.surf.api.minestom.extension.addListener
import dev.slne.surf.api.minestom.plugin.SurfMinestomPlugin
import dev.slne.surf.player.core.client.SurfPlayerClient
import dev.slne.surf.player.minestom.chat.ChatMinestomFeature
import dev.slne.surf.player.minestom.clan.ClanMinestomFeature
import dev.slne.surf.player.minestom.contentcreator.ContentCreatorMinestomFeature
import dev.slne.surf.player.minestom.friends.FriendsMinestomFeature
import dev.slne.surf.player.minestom.nametag.NametagMinestomFeature
import dev.slne.surf.player.minestom.tab.TabMinestomFeature
import kotlinx.coroutines.CancellationException
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.event.player.PlayerSpawnEvent

/**
 * The single Minestom plugin of surf-player. It hosts every social feature and drives their
 * lifecycle in dependency order: chat → friends → clan → contentcreator → nametag → tab
 * (disabled in reverse order).
 */
object PlayerMinestomPlugin : SurfMinestomPlugin() {
    private val features: List<MinestomPlayerFeature> = listOf(
        ChatMinestomFeature,
        FriendsMinestomFeature,
        ClanMinestomFeature,
        ContentCreatorMinestomFeature,
        NametagMinestomFeature,
        TabMinestomFeature,
    )

    override suspend fun onLoad() {
        // One shared RabbitMQ/Redis connection for every feature, see SurfPlayerClient.
        SurfPlayerClient.load(dataDirectory)
        SurfPlayerClient.connectRabbit()

        // Features register everything Redis needs here, before Redis is frozen.
        for (feature in features) {
            feature.onLoad()
        }
    }

    override suspend fun onEnable() {
        SurfPlayerClient.connectRedis()

        for (feature in features) {
            feature.onEnable()
        }

        registerPlayerConnectionListeners()
    }

    override suspend fun onDisable() {
        for (feature in features.asReversed()) {
            try {
                feature.onDisable()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.error("Failed to disable feature '${feature.id}'", e)
            }
        }

        SurfPlayerClient.disable()
    }

    private fun registerPlayerConnectionListeners() {
        eventNode.addListener<PlayerSpawnEvent> { event ->
            if (!event.isFirstSpawn) return@addListener

            forEachFeature("join") { it.onPlayerJoin(event.player) }
        }

        eventNode.addListener<PlayerDisconnectEvent> { event ->
            forEachFeature("quit") { it.onPlayerQuit(event.player) }
        }
    }

    /**
     * Runs [action] for every feature; a failing feature does not keep the others from running.
     */
    private inline fun forEachFeature(event: String, action: (MinestomPlayerFeature) -> Unit) {
        for (feature in features) {
            try {
                action(feature)
            } catch (e: Exception) {
                logger.error("Feature '${feature.id}' failed to handle a player $event", e)
            }
        }
    }
}
