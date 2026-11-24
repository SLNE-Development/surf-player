package dev.slne.surf.social.paper.clan

import dev.slne.surf.social.core.common.clan.ClanInstance
import dev.slne.surf.social.paper.PaperSocialFeature

object PaperClanFeature : PaperSocialFeature {
    override val name = "clan"

    override suspend fun onLoad() {
        ClanInstance.load()
    }

    override suspend fun onEnable() {
        ClanInstance.enable()
    }

    override suspend fun onDisable() {
        ClanInstance.disable()
    }
}
