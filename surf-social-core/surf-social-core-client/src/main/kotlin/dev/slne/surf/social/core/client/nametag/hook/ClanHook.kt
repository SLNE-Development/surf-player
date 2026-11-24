package dev.slne.surf.social.core.client.nametag.hook

import dev.slne.surf.social.api.clan.clan.Clan
import dev.slne.surf.social.api.clan.clan.ClanView
import dev.slne.surf.social.api.clan.clan.listener.ClanCreatedListener
import dev.slne.surf.social.api.clan.clan.listener.ClanDeletedListener
import dev.slne.surf.social.api.clan.clan.listener.ClanUpdateMemberListener
import dev.slne.surf.social.api.clan.clan.listener.ClanUpdatedListener
import dev.slne.surf.social.core.client.nametag.platform.NametagPlatform
import dev.slne.surf.social.core.client.nametag.platform.NametagPlayer
import dev.slne.surf.social.core.client.nametag.service.NametagService
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import java.util.*

object ClanHook {
    suspend fun getClanTag(playerUuid: UUID): Component =
        Clan.byPlayer(playerUuid)?.renderClanTag(1) ?: Component.empty()

    fun createListeners() {
        Clan.registerListener(ClanCreatedListener(::updateMembers))
        Clan.registerListener(ClanUpdatedListener(::updateMembers))
        Clan.registerListener(ClanDeletedListener(::updateMembers))
        Clan.registerListener(ClanUpdateMemberListener { clan, _, _ -> updateMembers(clan) })
    }

    private fun updateMembers(clan: ClanView) {
        NametagPlatform.launch {
            for (member in clan.members) {
                val player = NametagPlatform.player(member.uuid) ?: continue
                launch { updatePlayer(player) }
            }
        }
    }

    private suspend fun updatePlayer(player: NametagPlayer) {
        NametagService.handleDataUpdate(player)
    }
}
