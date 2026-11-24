package dev.slne.surf.social.minestom.clan

import dev.slne.surf.social.minestom.clan.command.clanChatCommand
import dev.slne.surf.social.minestom.clan.command.clanCommand
import dev.slne.surf.social.minestom.clan.listener.ClanTagColorJoinListener
import dev.slne.surf.social.minestom.MinestomSocialFeature
import dev.slne.surf.social.minestom.SocialMinestomPlugin
import dev.slne.surf.social.core.common.clan.ClanInstance

object ClanMinestomFeature : MinestomSocialFeature {
    override val id = "clan"

    val dataDirectory get() = SocialMinestomPlugin.featureDataDirectory(this)

    override suspend fun onLoad() {
        ClanInstance.load()
    }

    override suspend fun onEnable() {
        ClanInstance.enable()
        clanCommand()

        clanChatCommand("clanchat").register()
        clanChatCommand("cc").register()

        ClanTagColorJoinListener.register(SocialMinestomPlugin.eventNode)
    }

    override suspend fun onDisable() {
        ClanInstance.disable()
    }
}
