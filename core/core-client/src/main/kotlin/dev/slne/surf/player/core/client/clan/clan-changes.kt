package dev.slne.surf.player.core.client.clan

import dev.slne.surf.player.api.clan.SurfClanApi
import dev.slne.surf.player.api.clan.clan.ClanView
import dev.slne.surf.player.api.clan.clan.listener.ClanCreatedListener
import dev.slne.surf.player.api.clan.clan.listener.ClanDeletedListener
import dev.slne.surf.player.api.clan.clan.listener.ClanUpdateMemberListener
import dev.slne.surf.player.api.clan.clan.listener.ClanUpdatedListener

/**
 * Calls [onChange] whenever a clan is created, updated or deleted, or gains or loses a member.
 */
fun onEveryClanChange(onChange: (ClanView) -> Unit) {
    SurfClanApi.registerListener(ClanCreatedListener(onChange))
    SurfClanApi.registerListener(ClanUpdatedListener(onChange))
    SurfClanApi.registerListener(ClanDeletedListener(onChange))
    SurfClanApi.registerListener(ClanUpdateMemberListener { clan, _, _ -> onChange(clan) })
}
