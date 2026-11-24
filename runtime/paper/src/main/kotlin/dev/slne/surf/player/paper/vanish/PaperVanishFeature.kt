package dev.slne.surf.player.paper.vanish

import com.github.retrooper.packetevents.PacketEvents
import dev.slne.surf.api.paper.event.register
import dev.slne.surf.core.api.paper.CorePlayerStatusAccess
import dev.slne.surf.player.paper.PaperPlayerFeature
import dev.slne.surf.player.paper.vanish.command.vanishAdminActions
import dev.slne.surf.player.paper.vanish.command.vanishCommand
import dev.slne.surf.player.paper.vanish.config.initVanishConfig
import dev.slne.surf.player.paper.vanish.listener.*
import dev.slne.surf.player.paper.vanish.service.VanishService
import dev.slne.surf.player.paper.vanish.util.canVanishSee
import org.bukkit.Bukkit

/**
 * The former surf-vanish Paper plugin.
 */
object PaperVanishFeature : PaperPlayerFeature {
    override val name = "vanish"

    override suspend fun onLoad() {
        initVanishConfig(dataPath)
        VanishService.registerRedis()
    }

    override suspend fun onEnable() {
        ConnectionListener.register()
        SilencePlayerListener.register()
        AdvancementListener.register()
        GameModeChangeListener.register()
        TabCompleteListener.register()
        PacketEvents.getAPI().eventManager.registerListener(HotkeyListener)

        vanishCommand()

        CorePlayerStatusAccess.registerHandler { viewer, player ->
            if (!VanishService.isVanished(player.uuid)) {
                return@registerHandler true
            }

            val viewerPlayer = Bukkit.getPlayer(viewer.uuid) ?: return@registerHandler true
            val playerPlayer = Bukkit.getPlayer(player.uuid) ?: return@registerHandler true

            viewerPlayer.canVanishSee(playerPlayer)
        }

        VanishService.startActionbarTask()
    }

    override suspend fun onDisable() {
        VanishService.stopActionbarTask()
        PacketEvents.getAPI().eventManager.unregisterListener(HotkeyListener)
    }

    override fun adminActions() = vanishAdminActions()
}
