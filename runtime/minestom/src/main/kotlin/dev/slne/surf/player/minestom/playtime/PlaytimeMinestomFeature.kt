package dev.slne.surf.player.minestom.playtime

import dev.slne.surf.api.minestom.coroutine.minestomAsyncScope
import dev.slne.surf.player.core.client.playtime.PlaytimeTasks
import dev.slne.surf.player.core.client.playtime.config.PlaytimeConfigManager
import dev.slne.surf.player.core.client.playtime.session.endPlaytimeSessions
import dev.slne.surf.player.core.client.playtime.session.handlePlaytimeJoin
import dev.slne.surf.player.core.client.playtime.session.invalidatePlaytimeCaches
import dev.slne.surf.player.core.client.playtime.session.startPlaytimeSession
import dev.slne.surf.player.minestom.MinestomPlayerFeature
import dev.slne.surf.player.minestom.PlayerMinestomPlugin
import dev.slne.surf.player.minestom.playtime.command.playtimeAdminActions
import dev.slne.surf.player.minestom.playtime.command.playtimeCommand
import dev.slne.surf.player.minestom.playtime.listener.PlayerAfkListener
import kotlinx.coroutines.launch
import net.minestom.server.entity.Player

object PlaytimeMinestomFeature : MinestomPlayerFeature {
    override val id = "playtime"

    override suspend fun onLoad() {
        PlaytimeConfigManager.load(dataDirectory)
        PlaytimeTasks.start(minestomAsyncScope)
    }

    override suspend fun onEnable() {
        playtimeCommand()

        PlayerAfkListener.register(PlayerMinestomPlugin.eventNode)
    }

    override suspend fun onDisable() {
        PlaytimeTasks.stop()
    }

    override fun adminActions() = playtimeAdminActions()

    override fun onPlayerJoin(player: Player) {
        startPlaytimeSession(player.uuid)

        minestomAsyncScope.launch {
            handlePlaytimeJoin(player.uuid, player)
        }
    }

    override fun onPlayerQuit(player: Player) {
        val playerUuid = player.uuid
        invalidatePlaytimeCaches(playerUuid)

        minestomAsyncScope.launch {
            endPlaytimeSessions(playerUuid)
        }
    }
}
