package dev.slne.surf.player.minestom.clan

import dev.slne.surf.api.minestom.coroutine.minestomAsyncScope
import dev.slne.surf.player.core.client.clan.ClanClient
import dev.slne.surf.player.core.client.clan.tagcolor.ClanTagColorEnforcer
import dev.slne.surf.player.minestom.MinestomPlayerFeature
import dev.slne.surf.player.minestom.clan.command.clanChatCommand
import dev.slne.surf.player.minestom.clan.command.clanCommand
import kotlinx.coroutines.launch
import net.minestom.server.entity.Player

object ClanMinestomFeature : MinestomPlayerFeature {
    override val id = "clan"

    override suspend fun onLoad() {
        ClanClient.load(dataDirectory)
    }

    override suspend fun onEnable() {
        clanCommand()

        clanChatCommand("clanchat").register()
        clanChatCommand("cc").register()
    }

    /**
     * Checks the clan tag color of the joining player's clan against its owner's permissions.
     */
    override fun onPlayerJoin(player: Player) {
        val playerUuid = player.uuid

        minestomAsyncScope.launch {
            ClanTagColorEnforcer.enforceForClanOf(playerUuid)
        }
    }
}
