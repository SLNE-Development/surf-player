package dev.slne.surf.social.minestom.tab.platform

import dev.slne.surf.api.minestom.coroutine.minestomScope
import dev.slne.surf.api.minestom.extension.ConnectionManager
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.social.core.client.tab.platform.TabPlatform
import dev.slne.surf.social.minestom.tab.TabMinestomFeature
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*

class MinestomTabPlatform : TabPlatform {
    override val dataPath get() = TabMinestomFeature.dataDirectory

    override val playtimeAvailable = true

    override fun onlinePlayers() = ConnectionManager.onlinePlayers.map { MinestomTabPlayer(it) }

    override fun onlinePlayerCount() = ConnectionManager.onlinePlayers.size

    override fun player(playerUuid: UUID) =
        ConnectionManager.getOnlinePlayerByUuid(playerUuid)?.let { MinestomTabPlayer(it) }

    override fun maxPlayerCount() = SurfServer.current().maxPlayers

    override fun isVanished(playerUuid: UUID) = false

    override fun launch(block: suspend CoroutineScope.() -> Unit) = minestomScope.launch(block = block)
}
