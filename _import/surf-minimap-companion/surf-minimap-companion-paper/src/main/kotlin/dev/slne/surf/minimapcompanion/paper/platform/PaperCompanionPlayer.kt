package dev.slne.surf.minimapcompanion.paper.platform

import dev.slne.surf.minimapcompanion.core.platform.CompanionPlayer
import dev.slne.surf.minimapcompanion.paper.plugin
import org.bukkit.World
import org.bukkit.entity.Player

/**
 * Wraps [player] in [world], which defaults to the world they are in now.
 */
class PaperCompanionPlayer(
    private val player: Player,
    private val world: World = player.world,
) : CompanionPlayer {
    override val uuid get() = player.uniqueId
    override val worldKey get() = world.key
    override val worldIdentity get() = world.uid.toString()

    override fun sendPluginMessage(channel: String, payload: ByteArray) {
        player.sendPluginMessage(plugin, channel, payload)
    }
}
