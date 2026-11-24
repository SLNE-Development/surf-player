package dev.slne.surf.player.core.client.tab.hook

import dev.slne.surf.player.api.clan.SurfClanApi
import dev.slne.surf.player.api.clan.clan.ClanView
import dev.slne.surf.player.core.client.clan.onEveryClanChange
import dev.slne.surf.player.core.client.tab.service.TablistService
import net.kyori.adventure.text.Component
import java.util.*

object ClanHook {
    /**
     * The clan tag shown behind [playerUuid]'s name, or `null` when they are in no clan or their
     * clan's tag is not shown (too few active members and not whitelisted).
     */
    suspend fun getClanTag(playerUuid: UUID): Component? =
        SurfClanApi.findClanByPlayer(playerUuid)?.renderClanTag(10)?.takeUnless { it == Component.empty() }

    fun createListeners() {
        onEveryClanChange(::updateMembers)
    }

    private fun updateMembers(clan: ClanView) {
        for (member in clan.members) {
            TablistService.updateEntry(member.uuid)
        }
    }
}
