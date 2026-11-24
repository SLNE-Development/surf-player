package dev.slne.surf.player.core.client.nametag.hook

import dev.slne.surf.player.api.clan.clan.Clan
import dev.slne.surf.player.api.clan.clan.ClanView
import dev.slne.surf.player.core.client.clan.onEveryClanChange
import dev.slne.surf.player.core.client.nametag.platform.NametagPlatform
import dev.slne.surf.player.core.client.nametag.service.NametagService
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import java.util.*

object ClanHook {
    suspend fun getClanTag(playerUuid: UUID): Component =
        Clan.byPlayer(playerUuid)?.renderClanTag(1) ?: Component.empty()

    fun createListeners() {
        onEveryClanChange(::updateMembers)
    }

    private fun updateMembers(clan: ClanView) {
        NametagPlatform.launch {
            for (member in clan.members) {
                val player = NametagPlatform.player(member.uuid) ?: continue
                launch { NametagService.handleDataUpdate(player) }
            }
        }
    }
}
