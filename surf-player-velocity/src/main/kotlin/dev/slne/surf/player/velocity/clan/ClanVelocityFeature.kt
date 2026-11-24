package dev.slne.surf.player.velocity.clan

import dev.slne.surf.player.core.common.clan.ClanInstance
import dev.slne.surf.player.velocity.PlayerVelocityPlugin
import dev.slne.surf.player.velocity.VelocityPlayerFeature
import dev.slne.surf.player.velocity.clan.listener.JoinInviteListener
import dev.slne.surf.player.velocity.plugin

/**
 * The clan feature of [PlayerVelocityPlugin] (formerly the `surf-clan-velocity` plugin).
 */
object ClanVelocityFeature : VelocityPlayerFeature {
    override val id = "clan"

    override suspend fun onLoad() {
        ClanInstance.load()
    }

    override suspend fun onEnable() {
        ClanInstance.enable()

        plugin.eventManager.register(plugin, JoinInviteListener)
    }

    override suspend fun onDisable() {
        ClanInstance.disable()
    }
}
