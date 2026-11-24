package dev.slne.surf.player.paper.clan

import dev.slne.surf.player.core.common.clan.ClanInstance
import dev.slne.surf.player.paper.PaperPlayerFeature

object PaperClanFeature : PaperPlayerFeature {
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
