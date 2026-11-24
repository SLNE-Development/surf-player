package dev.slne.surf.social.minestom

import dev.slne.surf.api.minestom.plugin.SurfMinestomPlugin
import dev.slne.surf.social.core.client.SocialClient
import dev.slne.surf.social.minestom.chat.ChatMinestomFeature
import dev.slne.surf.social.minestom.clan.ClanMinestomFeature
import dev.slne.surf.social.minestom.contentcreator.ContentCreatorMinestomFeature
import dev.slne.surf.social.minestom.friends.FriendsMinestomFeature
import dev.slne.surf.social.minestom.nametag.NametagMinestomFeature
import dev.slne.surf.social.minestom.tab.TabMinestomFeature
import kotlinx.coroutines.CancellationException
import java.nio.file.Path
import kotlin.io.path.createDirectories

/**
 * The single Minestom plugin of surf-social. It hosts every social feature and drives their
 * lifecycle in dependency order: chat → friends → clan → contentcreator → nametag → tab
 * (disabled in reverse order).
 */
object SocialMinestomPlugin : SurfMinestomPlugin() {
    private val features: List<MinestomSocialFeature> = listOf(
        ChatMinestomFeature,
        FriendsMinestomFeature,
        ClanMinestomFeature,
        ContentCreatorMinestomFeature,
        NametagMinestomFeature,
        TabMinestomFeature,
    )

    /**
     * The data directory of [feature]: `plugins/<plugin id>/<feature id>`. Each feature gets its
     * own subfolder since several of them use the same file names (e.g. `config.yml`).
     */
    fun featureDataDirectory(feature: MinestomSocialFeature): Path =
        dataDirectory.resolve(feature.id).createDirectories()

    override suspend fun onLoad() {
        // One shared RabbitMQ/Redis connection for every feature, see SocialClient.
        SocialClient.load(dataDirectory)
        SocialClient.connectRabbit()

        // Features register everything Redis needs here, before Redis is frozen.
        for (feature in features) {
            feature.onLoad()
        }
    }

    override suspend fun onEnable() {
        SocialClient.connectRedis()

        for (feature in features) {
            feature.onEnable()
        }
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

        SocialClient.disable()
    }
}
