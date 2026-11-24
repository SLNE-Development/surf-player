package dev.slne.surf.social.minestom.clan

import dev.slne.surf.social.minestom.clan.command.clanChatCommand
import dev.slne.surf.social.minestom.clan.command.clanCommand
import dev.slne.surf.social.minestom.clan.listener.ClanTagColorJoinListener
import dev.slne.surf.api.minestom.plugin.SurfMinestomPlugin
import dev.slne.surf.social.core.common.clan.ClanInstance

object ClanMinestomPlugin : SurfMinestomPlugin() {
    override suspend fun onLoad() {
        ClanInstance.load()
    }

    override suspend fun onEnable() {
        ClanInstance.enable()
        clanCommand()

        clanChatCommand("clanchat").register()
        clanChatCommand("cc").register()

        ClanTagColorJoinListener.register(eventNode)
    }

    override suspend fun onDisable() {
        ClanInstance.disable()
    }
}
