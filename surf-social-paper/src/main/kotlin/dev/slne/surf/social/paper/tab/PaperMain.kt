package dev.slne.surf.social.paper.tab

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.api.paper.event.register
import dev.slne.surf.api.paper.extensions.pluginManager
import dev.slne.surf.social.core.client.tab.hook.ClanHook
import dev.slne.surf.social.core.client.tab.hook.ContentCreatorHook
import dev.slne.surf.social.core.client.tab.redis.redisLoader
import dev.slne.surf.social.core.client.tab.service.TablistService
import dev.slne.surf.social.paper.tab.command.surfTabCommand
import dev.slne.surf.social.paper.tab.hook.registerLuckPermsListeners
import dev.slne.surf.social.paper.tab.listener.PlayerListener
import dev.slne.surf.social.paper.tab.listener.PlaytimeListener
import dev.slne.surf.social.paper.tab.listener.VanishListener
import dev.slne.surf.social.paper.tab.platform.PaperTabPlatform
import org.bukkit.plugin.java.JavaPlugin

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)

class PaperMain : SuspendingJavaPlugin() {

    override fun onEnable() {
        redisLoader.connect()
        surfTabCommand()

        TablistService.start(PaperTabPlatform())
        registerLuckPermsListeners()
        PlayerListener.register()

        if (isPlaytimeHook) {
            PlaytimeListener.register()
        }

        if (isClansHook) {
            ClanHook.createListeners()
        }

        if (isContentCreatorHook) {
            ContentCreatorHook.registerListener()
        }
        if (isVanishHook) {
            VanishListener.register()
        }
    }

    override fun onDisable() {
        TablistService.stop()
        redisLoader.disconnect()
    }
}

val isVanishHook get() = pluginManager.isPluginEnabled("surf-vanish-paper")
val isPlaytimeHook get() = pluginManager.isPluginEnabled("surf-playtime-paper")
val isClansHook get() = pluginManager.isPluginEnabled("surf-clan-paper")
val isContentCreatorHook get() = pluginManager.isPluginEnabled("surf-content-creator-paper")
