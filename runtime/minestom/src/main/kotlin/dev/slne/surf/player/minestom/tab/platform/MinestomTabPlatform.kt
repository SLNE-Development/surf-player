package dev.slne.surf.player.minestom.tab.platform

import com.google.auto.service.AutoService
import dev.slne.surf.api.minestom.coroutine.minestomScope
import dev.slne.surf.api.minestom.extension.ConnectionManager
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.player.core.client.tab.platform.TabPlatform
import dev.slne.surf.player.minestom.tab.TabMinestomFeature
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*

@AutoService(TabPlatform::class)
class MinestomTabPlatform : TabPlatform {
    override val dataPath get() = TabMinestomFeature.dataDirectory

    override fun onlinePlayers() = ConnectionManager.onlinePlayers.map { MinestomTabPlayer(it) }

    override fun onlinePlayerCount() = ConnectionManager.onlinePlayers.size

    override fun player(playerUuid: UUID) =
        ConnectionManager.getOnlinePlayerByUuid(playerUuid)?.let { MinestomTabPlayer(it) }

    override fun maxPlayerCount() = SurfServer.current().maxPlayers

    override fun isVanished(playerUuid: UUID) = false

    override fun launch(block: suspend CoroutineScope.() -> Unit) = minestomScope.launch(block = block)
}
