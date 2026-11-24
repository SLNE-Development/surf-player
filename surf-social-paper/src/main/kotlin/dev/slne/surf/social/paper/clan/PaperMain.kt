package dev.slne.surf.social.paper.clan

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.api.paper.extensions.pluginManager
import dev.slne.surf.social.core.common.clan.ClanInstance
import org.bukkit.plugin.java.JavaPlugin

class PaperMain : SuspendingJavaPlugin() {
    override suspend fun onLoadAsync() {
        ClanInstance.load()
    }

    override suspend fun onEnableAsync() {
        ClanInstance.enable()
    }

    override suspend fun onDisableAsync() {
        ClanInstance.disable()
    }

    fun checkSurfChat() = pluginManager.isPluginEnabled("surf-chat-paper")
    fun checkLuckPerms() = pluginManager.isPluginEnabled("LuckPerms")
}

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)