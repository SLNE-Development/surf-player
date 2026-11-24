package dev.slne.surf.player.paper.playtime

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.api.paper.event.register
import dev.slne.surf.api.paper.extensions.pluginManager
import dev.slne.surf.player.core.client.playtime.ClientPlaytimeInstance
import dev.slne.surf.player.core.client.playtime.config.playtimeConfig
import dev.slne.surf.player.core.common.playtime.service.PayCheckService
import dev.slne.surf.player.core.common.playtime.service.PlaytimeService
import dev.slne.surf.player.paper.playtime.command.playtimeAdminCommand
import dev.slne.surf.player.paper.playtime.command.playtimeCommand
import dev.slne.surf.player.paper.playtime.listener.PlayerAfkListener
import dev.slne.surf.player.paper.playtime.listener.PlayerJoinListener
import dev.slne.surf.player.paper.playtime.listener.PlayerQuitListener
import dev.slne.surf.player.paper.playtime.playtime.playtimeTasks
import org.bukkit.plugin.java.JavaPlugin

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)

class PaperMain : SuspendingJavaPlugin() {
    override suspend fun onLoadAsync() {
        ClientPlaytimeInstance.clientLoader.onLoad()
    }

    override suspend fun onEnableAsync() {
        ClientPlaytimeInstance.clientLoader.onEnable()

        PayCheckService.create(playtimeConfig)

        PlayerJoinListener.register()
        PlayerQuitListener.register()
        PlayerAfkListener.register()
        PlayerAfkListener.startAfkCheckTask()
        playtimeTasks.startAll()

        playtimeCommand()
        playtimeAdminCommand()
    }

    override suspend fun onDisableAsync() {
        playtimeTasks.stopAll()
        PlaytimeService.flushAll()

        ClientPlaytimeInstance.clientLoader.onDisable()
    }
}

val hasTransactionHook get() = pluginManager.isPluginEnabled("surf-transaction-paper")
