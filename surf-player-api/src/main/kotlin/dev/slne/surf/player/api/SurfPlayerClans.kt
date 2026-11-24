package dev.slne.surf.player.api

import dev.slne.surf.player.api.clan.clan.ClanService
import dev.slne.surf.player.api.clan.invite.ClanInviteService
import dev.slne.surf.player.api.clan.member.ClanMemberService
import dev.slne.surf.player.api.clan.player.ClanPlayerService

/** The clan services, reached through [SurfPlayerApi.clans]. */
object SurfPlayerClans {
    val clans: ClanService get() = ClanService.INSTANCE
    val members: ClanMemberService get() = ClanMemberService.INSTANCE
    val invites: ClanInviteService get() = ClanInviteService.INSTANCE
    val players: ClanPlayerService get() = ClanPlayerService.INSTANCE
}
