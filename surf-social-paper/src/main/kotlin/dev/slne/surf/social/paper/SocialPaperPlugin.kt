package dev.slne.surf.social.paper

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.social.paper.chat.PaperChatFeature
import dev.slne.surf.social.paper.clan.PaperClanFeature
import dev.slne.surf.social.paper.friends.PaperFriendsFeature
import dev.slne.surf.social.paper.shrieker.PaperShriekerFeature
import dev.slne.surf.social.paper.tab.PaperTabFeature
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level

val plugin get() = JavaPlugin.getPlugin(SocialPaperPlugin::class.java)

class SocialPaperPlugin : SuspendingJavaPlugin() {

    /**
     * Load/enable order. Clan hooks into chat, tab hooks into clan,
     * so dependencies come first. Disabling happens in reverse order.
     */
    private val features: List<PaperFeature> = listOf(
        PaperChatFeature,
        PaperFriendsFeature,
        PaperClanFeature,
        PaperShriekerFeature,
        PaperTabFeature,
    )

    override suspend fun onLoadAsync() {
        for (feature in features) {
            feature.onLoad()
        }
    }

    override suspend fun onEnableAsync() {
        for (feature in features) {
            feature.onEnable()
        }
    }

    override suspend fun onDisableAsync() {
        for (feature in features.asReversed()) {
            try {
                feature.onDisable()
            } catch (e: Exception) {
                logger.log(Level.SEVERE, "Failed to disable feature ${feature.name}", e)
            }
        }
    }
}
