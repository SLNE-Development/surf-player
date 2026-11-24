package dev.slne.surf.social.velocity.clan

import com.google.auto.service.AutoService
import dev.slne.surf.social.velocity.clan.listener.JoinInviteListener
import dev.slne.surf.social.velocity.plugin
import dev.slne.surf.social.core.common.clan.ClanInstance
import dev.slne.surf.social.core.client.clan.ClientClanInstance

@AutoService(ClanInstance::class)
class VelocityClanInstance : ClientClanInstance() {
    override val dataPath get() = ClanVelocityFeature.dataDirectory

    override suspend fun enable() {
        super.enable()

        val eventManager = plugin.eventManager
        eventManager.register(plugin, JoinInviteListener)
    }
}