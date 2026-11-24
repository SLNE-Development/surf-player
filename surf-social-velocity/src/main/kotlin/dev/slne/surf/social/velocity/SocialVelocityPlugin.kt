package dev.slne.surf.social.velocity

import com.github.shynixn.mccoroutine.velocity.SuspendingPluginContainer
import com.google.inject.Inject
import com.velocitypowered.api.event.EventManager
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.PluginContainer
import com.velocitypowered.api.plugin.annotation.DataDirectory
import dev.slne.surf.social.core.client.SocialClient
import dev.slne.surf.social.velocity.clan.ClanVelocityFeature
import dev.slne.surf.social.velocity.connection.ConnectionVelocityFeature
import kotlinx.coroutines.CancellationException
import org.slf4j.Logger
import java.nio.file.Path

val plugin get() = SocialVelocityPlugin.instance

/**
 * The single Velocity plugin of surf-social. It hosts every social feature that has a proxy side
 * and forwards the plugin lifecycle to them.
 */
class SocialVelocityPlugin @Inject constructor(
    val eventManager: EventManager,
    val container: PluginContainer,
    val logger: Logger,
    @param:DataDirectory val dataPath: Path,
    suspendingPluginContainer: SuspendingPluginContainer
) {
    private val features: List<VelocitySocialFeature> = listOf(
        ClanVelocityFeature,
        ConnectionVelocityFeature,
    )

    init {
        instance = this
        suspendingPluginContainer.initialize(this)
    }

    @Subscribe
    suspend fun onProxyInitialization(event: ProxyInitializeEvent) {
        // One shared RabbitMQ/Redis connection for every feature, see SocialClient.
        SocialClient.load(dataPath)
        SocialClient.connectRabbit()

        // Features register everything Redis needs here, before Redis is frozen.
        for (feature in features) {
            feature.onLoad()
        }

        SocialClient.connectRedis()

        for (feature in features) {
            feature.onEnable()
        }
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

        SocialClient.disable()
    }

    companion object {
        lateinit var instance: SocialVelocityPlugin
            private set
    }
}
