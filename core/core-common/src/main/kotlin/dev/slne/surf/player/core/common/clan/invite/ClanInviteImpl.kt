package dev.slne.surf.player.core.common.clan.invite

import dev.slne.surf.player.api.clan.invite.ClanInvite
import dev.slne.surf.player.api.clan.invite.ClanInviteAcceptResult
import dev.slne.surf.player.api.clan.invite.ClanInviteView
import dev.slne.surf.player.core.common.clan.CoreClanApi
import dev.slne.surf.api.core.serializer.java.datetime.datetime.offset.SerializableOffsetDateTime
import dev.slne.surf.api.core.serializer.java.uuid.SerializableStringUUID
import kotlinx.serialization.Serializable

@Serializable
data class ClanInviteImpl(
    val id: ULong,
    override val clanID: ULong,
    override val invited: SerializableStringUUID,
    override val invitedBy: SerializableStringUUID,

    override val createdAt: SerializableOffsetDateTime,
    val updatedAt: SerializableOffsetDateTime?
) : AbstractClanInviteView(), ClanInvite {
    override suspend fun accept(): ClanInviteAcceptResult {
        return CoreClanApi.acceptInvite(this)
    }

    override suspend fun revoke(): Boolean {
        return CoreClanApi.revokeInvite(this)
    }

    override fun view(): ClanInviteView = ClanInviteViewImpl(
        clanID = clanID,
        invited = invited,
        invitedBy = invitedBy,
        createdAt = createdAt
    )

}