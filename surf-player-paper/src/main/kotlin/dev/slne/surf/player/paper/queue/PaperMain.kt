package dev.slne.surf.player.paper.queue

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.player.core.common.queue.QueueInstance
import org.bukkit.plugin.java.JavaPlugin

class PaperMain : SuspendingJavaPlugin() {
    override suspend fun onLoadAsync() {
        QueueInstance.get().load()
    }

    override suspend fun onEnableAsync() {
        QueueInstance.get().enable()
    }

    override suspend fun onDisableAsync() {
        QueueInstance.get().disable()
    }
}

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)