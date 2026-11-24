package dev.slne.surf.social.paper.vanish

import com.github.retrooper.packetevents.PacketEvents
import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.api.paper.event.register
import dev.slne.surf.core.api.paper.CorePlayerStatusAccess
import dev.slne.surf.social.paper.vanish.core.redisLoader
import dev.slne.surf.social.paper.vanish.core.service.vanishService
import dev.slne.surf.social.paper.vanish.command.nickCommand
import dev.slne.surf.social.paper.vanish.command.vanishCommand
import dev.slne.surf.social.paper.vanish.config.VanishConfiguration
import dev.slne.surf.social.paper.vanish.listener.*
import dev.slne.surf.social.paper.vanish.service.VanishServiceImpl
import dev.slne.surf.social.paper.vanish.util.canVanishSee
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)

class PaperMain : SuspendingJavaPlugin() {
    override fun onEnable() {
        ConnectionListener.register()
        SilencePlayerListener.register()
        AdvancementListener.register()
        GameModeChangeListener.register()
        TabCompleteListener.register()
        PacketEvents.getAPI().eventManager.registerListener(HotkeyListener)

        redisLoader.connect()

        vanishCommand()
        nickCommand()

        CorePlayerStatusAccess.registerHandler { viewer, player ->
            if (!vanishService.isVanished(player.uuid)) {
                return@registerHandler true
            }

            val viewerPlayer = Bukkit.getPlayer(viewer.uuid) ?: return@registerHandler true
            val playerPlayer = Bukkit.getPlayer(player.uuid) ?: return@registerHandler true

            viewerPlayer.canVanishSee(playerPlayer)
        }

        VanishServiceImpl.startTask()
    }

    override fun onDisable() {
        VanishServiceImpl.stopTask()

        redisLoader.disconnect()
    }

    val vanishConfig = VanishConfiguration()
}

val config get() = plugin.vanishConfig.config