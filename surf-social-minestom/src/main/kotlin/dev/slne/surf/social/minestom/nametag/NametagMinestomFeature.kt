package dev.slne.surf.social.minestom.nametag

import dev.slne.surf.social.core.client.nametag.config.NametagConfig
import dev.slne.surf.social.core.client.nametag.hook.ClanHook
import dev.slne.surf.social.core.client.nametag.hook.ContentCreatorHook
import dev.slne.surf.social.minestom.MinestomSocialFeature
import dev.slne.surf.social.minestom.SocialMinestomPlugin
import dev.slne.surf.social.minestom.nametag.hook.registerLuckPermsListeners
import dev.slne.surf.social.minestom.nametag.listener.NametagListener

/**
 * The former surf-nametag Minestom plugin. Clans and content creators are bundled in the same
 * plugin and set up before this feature.
 */
object NametagMinestomFeature : MinestomSocialFeature {
    override val id = "nametag"

    val dataDirectory get() = SocialMinestomPlugin.featureDataDirectory(this)

    override suspend fun onLoad() {
        NametagConfig.init()

        registerLuckPermsListeners()
        ClanHook.createListeners()
        ContentCreatorHook.registerListener()
    }

    override suspend fun onEnable() {
        NametagListener.register(SocialMinestomPlugin.eventNode)
    }
}
