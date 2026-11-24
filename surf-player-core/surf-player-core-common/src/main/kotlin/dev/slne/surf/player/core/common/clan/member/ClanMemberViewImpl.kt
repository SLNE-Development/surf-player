package dev.slne.surf.player.core.common.clan.member

import dev.slne.surf.player.api.clan.member.ClanMemberRole
import java.time.OffsetDateTime
import java.util.*

data class ClanMemberViewImpl(
    override val uuid: UUID,
    override val role: ClanMemberRole,
    override val addedBy: UUID?,
    override val lastActiveAt: OffsetDateTime
) : AbstractClanMemberView()