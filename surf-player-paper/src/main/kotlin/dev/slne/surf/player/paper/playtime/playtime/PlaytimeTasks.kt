package dev.slne.surf.player.paper.playtime.playtime

import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.player.core.common.playtime.service.PlaytimeService
import dev.slne.surf.player.paper.playtime.plugin
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.Bukkit
import java.util.concurrent.TimeUnit

val playtimeTasks = PlaytimeTasks()

class PlaytimeTasks {
    lateinit var playtimeTask: ScheduledTask
    lateinit var flushAllTask: ScheduledTask

    fun startAll() {
        playtimeTask = playTimeTask()
        flushAllTask = flushAllTask()
    }

    fun stopAll() {
        playtimeTask.cancel()
        flushAllTask.cancel()
    }

    private fun playTimeTask() = Bukkit.getAsyncScheduler().runAtFixedRate(plugin, {
        plugin.launch {
            PlaytimeService.updateAllActiveSessions()
        }


    }, 0L, 1L, TimeUnit.SECONDS)

    private fun flushAllTask() = Bukkit.getAsyncScheduler().runAtFixedRate(plugin, {
        plugin.launch {
            PlaytimeService.flushAll()
        }
    }, 0L, 5L, TimeUnit.MINUTES)
}