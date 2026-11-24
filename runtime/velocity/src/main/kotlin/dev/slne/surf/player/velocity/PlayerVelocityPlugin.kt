package dev.slne.surf.player.velocity

import com.github.shynixn.mccoroutine.velocity.SuspendingPluginContainer
import com.google.inject.Inject
import com.velocitypowered.api.event.EventManager
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.PluginContainer
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import dev.slne.surf.player.core.client.SurfPlayerClient
import dev.slne.surf.player.velocity.clan.ClanVelocityFeature
import dev.slne.surf.player.velocity.command.VelocityAdminCommand
import dev.slne.surf.player.velocity.connection.ConnectionVelocityFeature
import dev.slne.surf.player.velocity.friends.FriendsVelocityFeature
import dev.slne.surf.player.velocity.queue.QueueVelocityFeature
import dev.slne.surf.player.velocity.settings.SettingsVelocityFeature
import kotlinx.coroutines.CancellationException
import org.slf4j.Logger
import java.nio.file.Path

val plugin get() = PlayerVelocityPlugin.instance

/**
 * The single Velocity plugin of surf-player. It hosts every social feature that has a proxy side
 * and forwards the plugin lifecycle to them.
 */
class PlayerVelocityPlugin @Inject constructor(
    val proxy: ProxyServer,
    val eventManager: EventManager,
    val container: PluginContainer,
    val logger: Logger,
    @param:DataDirectory val dataPath: Path,
    suspendingPluginContainer: SuspendingPluginContainer
) {
    private val features: List<VelocityPlayerFeature> = listOf(
        SettingsVelocityFeature,
        ClanVelocityFeature,
        FriendsVelocityFeature,
        ConnectionVelocityFeature,
        QueueVelocityFeature,
    )

    init {
        instance = this
        suspendingPluginContainer.initialize(this)
    }

    @Subscribe
    suspend fun onProxyInitialization(event: ProxyInitializeEvent) {
        // One shared RabbitMQ/Redis connection for every feature, see SurfPlayerClient.
        SurfPlayerClient.load(dataPath)
        SurfPlayerClient.connectRabbit()

        // Features register everything Redis needs here, before Redis is frozen.
        for (feature in features) {
            feature.onLoad()
        }

        SurfPlayerClient.connectRedis()

        for (feature in features) {
            feature.onEnable()
        }

        VelocityAdminCommand.register(features)
    }

    @Subscribe
    suspend fun onProxyShutdown(event: ProxyShutdownEvent) {
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

    companion object {
        lateinit var instance: PlayerVelocityPlugin
            private set
    }
}
