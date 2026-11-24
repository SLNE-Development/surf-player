package dev.slne.surf.social.velocity

import com.github.shynixn.mccoroutine.velocity.SuspendingPluginContainer
import com.google.inject.Inject
import com.velocitypowered.api.event.EventManager
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.PluginContainer
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import dev.slne.surf.social.core.client.SocialClient
import dev.slne.surf.social.velocity.clan.ClanVelocityFeature
import dev.slne.surf.social.velocity.connection.ConnectionVelocityFeature
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import java.nio.file.Path
import kotlin.io.path.createDirectories

val plugin get() = SocialVelocityPlugin.instance

/**
 * The single Velocity plugin of surf-social. It hosts every social feature that has a proxy side
 * and forwards the plugin lifecycle to them.
 */
class SocialVelocityPlugin @Inject constructor(
    val proxy: ProxyServer,
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

        runBlocking {
            SocialClient.load(dataPath)
            SocialClient.connectRabbit()

            for (feature in features) {
                feature.onLoad()
            }
        }
    }

    /**
     * The data directory of [feature]: `plugins/<plugin id>/<feature id>`.
     */
    fun featureDataDirectory(feature: VelocitySocialFeature): Path =
        dataPath.resolve(feature.id).createDirectories()

    @Subscribe
    suspend fun onProxyInitialization(event: ProxyInitializeEvent) {
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
    }
}
