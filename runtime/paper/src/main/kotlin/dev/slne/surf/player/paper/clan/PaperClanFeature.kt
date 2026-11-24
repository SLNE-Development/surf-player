package dev.slne.surf.player.paper.clan

import dev.slne.surf.api.paper.event.register
import dev.slne.surf.player.core.client.clan.ClanClient
import dev.slne.surf.player.paper.PaperPlayerFeature
import dev.slne.surf.player.paper.clan.commands.clanCommand
import dev.slne.surf.player.paper.clan.commands.subcommands.ClanChatCommand
import dev.slne.surf.player.paper.clan.listener.ClanTagColorJoinListener
import dev.slne.surf.player.paper.plugin

object PaperClanFeature : PaperPlayerFeature {
    override val name = "clan"

    override suspend fun onLoad() {
        ClanClient.load(dataPath)
    }

    override suspend fun onEnable() {
        clanCommand()

        ClanTagColorJoinListener.register(plugin)

        ClanChatCommand("clanchat").register()
        ClanChatCommand("cc").register()
    }
}
