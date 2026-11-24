package dev.slne.surf.player.core.common.clan.member

import dev.slne.surf.player.api.clan.clan.Clan
import dev.slne.surf.player.api.clan.member.ClanMemberView
import dev.slne.surf.player.api.clan.permission.ClanPermission
import java.time.OffsetDateTime

abstract class AbstractClanMemberView : ClanMemberView {

    override fun isActiveAt(now: OffsetDateTime): Boolean {
        return !lastActiveAt.isBefore(now.minusSeconds(Clan.INACTIVE_AFTER.inWholeSeconds))
    }

    override fun hasPermission(clanPermission: ClanPermission): Boolean {
        return role.hasPermission(clanPermission)
    }
}