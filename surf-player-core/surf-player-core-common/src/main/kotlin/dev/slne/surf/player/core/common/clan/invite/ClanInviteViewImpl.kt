package dev.slne.surf.player.core.common.clan.invite

import java.time.OffsetDateTime
import java.util.*

data class ClanInviteViewImpl(
    override val clanID: ULong,
    override val invited: UUID,
    override val invitedBy: UUID,
    override val createdAt: OffsetDateTime
) : AbstractClanInviteView()