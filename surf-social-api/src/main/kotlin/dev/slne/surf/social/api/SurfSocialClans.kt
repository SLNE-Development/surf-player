package dev.slne.surf.social.api

import dev.slne.surf.social.api.clan.clan.ClanService
import dev.slne.surf.social.api.clan.invite.ClanInviteService
import dev.slne.surf.social.api.clan.member.ClanMemberService
import dev.slne.surf.social.api.clan.player.ClanPlayerService

/** The clan services, reached through [SurfSocialApi.clans]. */
object SurfSocialClans {
    val clans: ClanService get() = ClanService.INSTANCE
    val members: ClanMemberService get() = ClanMemberService.INSTANCE
    val invites: ClanInviteService get() = ClanInviteService.INSTANCE
    val players: ClanPlayerService get() = ClanPlayerService.INSTANCE
}
