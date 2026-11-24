package dev.slne.surf.player.paper

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.player.core.client.SurfPlayerClient
import dev.slne.surf.player.paper.chat.PaperChatFeature
import dev.slne.surf.player.paper.clan.PaperClanFeature
import dev.slne.surf.player.paper.command.PaperAdminCommand
import dev.slne.surf.player.paper.contentcreator.PaperContentCreatorFeature
import dev.slne.surf.player.paper.friends.PaperFriendsFeature
import dev.slne.surf.player.paper.minimap.PaperMinimapFeature
import dev.slne.surf.player.paper.nametag.PaperNametagFeature
import dev.slne.surf.player.paper.playtime.PaperPlaytimeFeature
import dev.slne.surf.player.paper.profile.PaperProfileFeature
import dev.slne.surf.player.paper.queue.PaperQueueFeature
import dev.slne.surf.player.paper.settings.PaperSettingsFeature
import dev.slne.surf.player.paper.shrieker.PaperShriekerFeature
import dev.slne.surf.player.paper.tab.PaperTabFeature
import dev.slne.surf.player.paper.vanish.PaperVanishFeature
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level

val plugin get() = JavaPlugin.getPlugin(PlayerPaperPlugin::class.java)

class PlayerPaperPlugin : SuspendingJavaPlugin() {

    /**
     * Load/enable order. Clan hooks into chat; nametag and tab hook into clan and
     * content creator, so dependencies come first. Disabling happens in reverse order.
     */
    private val features: List<PaperPlayerFeature> = listOf(
        PaperSettingsFeature,
        PaperPlaytimeFeature,
        PaperVanishFeature,
        PaperChatFeature,
        PaperFriendsFeature,
        PaperClanFeature,
        PaperShriekerFeature,
        PaperContentCreatorFeature,
        PaperNametagFeature,
        PaperTabFeature,
        PaperProfileFeature,
        PaperMinimapFeature,
        PaperQueueFeature,
    )

    override suspend fun onLoadAsync() {
        // One shared RabbitMQ/Redis connection for every feature, see SurfPlayerClient.
        SurfPlayerClient.load(dataPath)
        SurfPlayerClient.connectRabbit()

        // Features register everything Redis needs here, before Redis is frozen.
        for (feature in features) {
            feature.onLoad()
        }
    }

    override suspend fun onEnableAsync() {
        SurfPlayerClient.connectRedis()

        for (feature in features) {
            feature.onEnable()
        }

        PaperAdminCommand.register(features)
    }

    override suspend fun onDisableAsync() {
        for (feature in features.asReversed()) {
            try {
                feature.onDisable()
            } catch (e: Exception) {
                logger.log(Level.SEVERE, "Failed to disable feature ${feature.name}", e)
            }
        }

        SurfPlayerClient.disable()
    }
}
