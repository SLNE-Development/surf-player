package dev.slne.surf.social.core.common.clan.member

import dev.slne.surf.social.api.clan.clan.Clan
import dev.slne.surf.social.api.clan.member.ClanMemberView
import dev.slne.surf.social.api.clan.permission.ClanPermission
import java.time.OffsetDateTime

abstract class AbstractClanMemberView : ClanMemberView {

    override fun isActiveAt(now: OffsetDateTime): Boolean {
        return !lastActiveAt.isBefore(now.minusSeconds(Clan.INACTIVE_AFTER.inWholeSeconds))
    }

    override fun hasPermission(clanPermission: ClanPermission): Boolean {
        return role.hasPermission(clanPermission)
    }
}