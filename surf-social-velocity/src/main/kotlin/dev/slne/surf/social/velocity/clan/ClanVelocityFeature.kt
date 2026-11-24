package dev.slne.surf.social.velocity.clan

import dev.slne.surf.social.core.common.clan.ClanInstance
import dev.slne.surf.social.velocity.SocialVelocityPlugin
import dev.slne.surf.social.velocity.VelocitySocialFeature
import dev.slne.surf.social.velocity.plugin

/**
 * The clan feature of [SocialVelocityPlugin] (formerly the `surf-clan-velocity` plugin).
 */
object ClanVelocityFeature : VelocitySocialFeature {
    override val id = "clan"

    val dataDirectory get() = plugin.featureDataDirectory(this)

    override suspend fun onLoad() {
        ClanInstance.load()
    }

    override suspend fun onEnable() {
        ClanInstance.enable()
    }

    override suspend fun onDisable() {
        ClanInstance.disable()
    }
}
