package dev.slne.surf.player.paper.playtime

import com.github.shynixn.mccoroutine.folia.scope
import dev.slne.surf.api.paper.event.register
import dev.slne.surf.player.core.client.playtime.PlaytimeTasks
import dev.slne.surf.player.core.client.playtime.config.PlaytimeConfigManager
import dev.slne.surf.player.paper.PaperPlayerFeature
import dev.slne.surf.player.paper.playtime.command.playtimeAdminCommand
import dev.slne.surf.player.paper.playtime.command.playtimeCommand
import dev.slne.surf.player.paper.playtime.listener.PlaytimeListener
import dev.slne.surf.player.paper.plugin

object PaperPlaytimeFeature : PaperPlayerFeature {
    override val name = "playtime"

    override suspend fun onLoad() {
        PlaytimeConfigManager.load(dataPath)
    }

    override suspend fun onEnable() {
        PlaytimeListener.register()
        PlaytimeTasks.start(plugin.scope)

        playtimeCommand()
        playtimeAdminCommand()
    }

    override suspend fun onDisable() {
        PlaytimeTasks.stop()
    }
}
