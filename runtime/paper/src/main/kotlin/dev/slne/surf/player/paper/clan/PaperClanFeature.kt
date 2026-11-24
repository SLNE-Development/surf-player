package dev.slne.surf.player.paper.clan

import dev.slne.surf.api.paper.event.register
import dev.slne.surf.player.core.client.clan.ClanClient
import dev.slne.surf.player.paper.PaperPlayerFeature
import dev.slne.surf.player.paper.clan.command.clanAdminActions
import dev.slne.surf.player.paper.clan.command.registerClanCommands
import dev.slne.surf.player.paper.clan.hook.PaperClanPlaceholders
import dev.slne.surf.player.paper.clan.listener.ClanTagColorJoinListener
import dev.slne.surf.player.paper.plugin

object PaperClanFeature : PaperPlayerFeature {
    override val name = "clan"

    override suspend fun onLoad() {
        ClanClient.load(dataPath)
    }

    override suspend fun onEnable() {
        registerClanCommands()

        ClanTagColorJoinListener.register(plugin)

        PaperClanPlaceholders.registerExpansion()
        PaperClanPlaceholders.register(plugin)
    }

    override suspend fun onDisable() {
        PaperClanPlaceholders.unregisterExpansion()
    }

    override fun adminActions() = clanAdminActions()
}
