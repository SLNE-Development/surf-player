package dev.slne.surf.player.minestom.nametag

import dev.slne.surf.api.minestom.coroutine.minestomScope
import dev.slne.surf.player.core.client.nametag.config.NametagConfig
import dev.slne.surf.player.core.client.nametag.hook.ClanHook
import dev.slne.surf.player.core.client.nametag.hook.ContentCreatorHook
import dev.slne.surf.player.core.client.nametag.hook.LuckPermsHook
import dev.slne.surf.player.core.client.nametag.service.NametagService
import dev.slne.surf.player.minestom.MinestomPlayerFeature
import dev.slne.surf.player.minestom.hook.subscribeToLuckPermsUserChanges
import dev.slne.surf.player.minestom.nametag.platform.MinestomNametagPlayer
import kotlinx.coroutines.launch
import net.minestom.server.entity.Player

/**
 * The former surf-nametag Minestom plugin. Clans and content creators are bundled in the same
 * plugin and set up before this feature.
 */
object NametagMinestomFeature : MinestomPlayerFeature {
    override val id = "nametag"

    override suspend fun onLoad() {
        NametagConfig.init()

        subscribeToLuckPermsUserChanges(LuckPermsHook::updatePlayer)
        ClanHook.createListeners()
        ContentCreatorHook.registerListener()
    }

    override fun onPlayerJoin(player: Player) {
        val nametagPlayer = MinestomNametagPlayer(player)

        minestomScope.launch {
            NametagService.handleJoin(nametagPlayer)
        }
    }

    override fun onPlayerQuit(player: Player) {
        val nametagPlayer = MinestomNametagPlayer(player)

        minestomScope.launch {
            NametagService.handleLeave(nametagPlayer)
        }
    }
}
