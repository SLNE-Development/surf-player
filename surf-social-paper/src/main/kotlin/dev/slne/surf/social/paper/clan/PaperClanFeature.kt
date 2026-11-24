package dev.slne.surf.social.paper.clan

import dev.slne.surf.api.paper.extensions.pluginManager
import dev.slne.surf.social.core.common.clan.ClanInstance
import dev.slne.surf.social.paper.PaperFeature

object PaperClanFeature : PaperFeature {
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

    fun checkLuckPerms() = pluginManager.isPluginEnabled("LuckPerms")
}
