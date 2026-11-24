package dev.slne.surf.social.paper.nametag

import dev.slne.surf.api.paper.event.register
import dev.slne.surf.social.core.client.nametag.config.NametagConfig
import dev.slne.surf.social.core.client.nametag.hook.ClanHook
import dev.slne.surf.social.core.client.nametag.hook.ContentCreatorHook
import dev.slne.surf.social.core.client.nametag.hook.LuckPermsHook
import dev.slne.surf.social.paper.PaperSocialFeature
import dev.slne.surf.social.paper.hook.subscribeToLuckPermsUserChanges
import dev.slne.surf.social.paper.nametag.listener.NametagListener

/**
 * The former surf-nametag Paper plugin.
 *
 * Clans and content creators are bundled in the same plugin and enabled before this feature,
 * so their hooks are always registered.
 */
object PaperNametagFeature : PaperSocialFeature {
    override val name = "nametag"

    override suspend fun onEnable() {
        NametagConfig.init()
        NametagListener.register()

        subscribeToLuckPermsUserChanges(LuckPermsHook::updatePlayer)

        ClanHook.createListeners()
        ContentCreatorHook.registerListener()
    }
}
