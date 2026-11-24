package dev.slne.surf.social.core.common.clan.clan

import dev.slne.surf.social.api.clan.clan.Clan
import dev.slne.surf.social.api.clan.clan.ClanTagColor
import dev.slne.surf.social.api.clan.clan.ClanTagColorUpdate
import dev.slne.surf.social.api.clan.invite.ClanInvite
import dev.slne.surf.social.api.clan.invite.ClanInviteResult
import dev.slne.surf.social.api.clan.member.ClanMember
import dev.slne.surf.social.api.clan.member.ClanMemberAddResult
import dev.slne.surf.social.api.clan.member.ClanMemberRole
import dev.slne.surf.social.api.clan.util.InternalClanApi
import dev.slne.surf.api.core.util.freeze
import dev.slne.surf.api.core.util.mutableObjectSetOf
import dev.slne.surf.social.core.common.clan.member.ClanMemberImpl
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime
import java.util.*

@Serializable
data class ClanImpl(
    override val clanID: ULong,
    override val uuid: @Contextual UUID,
    override val name: String,
    override val tag: String,
    override val createdByUuid: @Contextual UUID,
    @field:Volatile override var clanTagColor: ClanTagColor?,
    @field:Volatile override var description: String?,
    @field:Volatile override var discordInvite: String?,
    @field:Volatile override var members: Set<ClanMemberImpl>,
    override val updatedAt: @Contextual OffsetDateTime,
    override val createdAt: @Contextual OffsetDateTime,
) : AbstractClanView(), Clan {
    override suspend fun setDescription(description: String?) {
        CoreClanService.updateDescription(this, description)
    }

    override suspend fun setDiscordInvite(discordInvite: String?) {
        CoreClanService.updateDiscordInvite(this, discordInvite)
    }

    override suspend fun changeClanTagColor(update: ClanTagColorUpdate) {
        CoreClanService.updateTagColor(this, update)
    }

    override suspend fun getPendingInvites(): Set<ClanInvite> {
        return CoreClanService.fetchPendingInvites(this)
    }

    override suspend fun invite(invitee: UUID, invitedBy: UUID): ClanInviteResult {
        return CoreClanService.invitePlayer(this, invitee, invitedBy)
    }

    override suspend fun revokeInvite(uuid: UUID): Boolean {
        return CoreClanService.revokeInvite(this, uuid)
    }

    override suspend fun addMember(
        uuid: UUID,
        role: ClanMemberRole,
        addedBy: UUID?
    ): ClanMemberAddResult {
        return CoreClanService.addMember(this, uuid, role, addedBy)
    }

    override suspend fun removeMember(member: ClanMember): Boolean {
        return removeMember(member.uuid)
    }

    override suspend fun removeMember(uuid: UUID): Boolean {
        return CoreClanService.removeMember(this, uuid)
    }

    override fun getMember(uuid: UUID): ClanMember? {
        return members.find { member -> member.uuid == uuid }
    }

    fun addMemberLocally(member: ClanMemberImpl): Unit = synchronized(this) {
        members = mutableObjectSetOf(members).apply { add(member) }.freeze()
    }

    fun removeMemberLocally(uuid: UUID): Unit = synchronized(this) {
        members = members.filterNotTo(mutableObjectSetOf()) { it.uuid == uuid }.freeze()
    }

    override suspend fun delete(): Boolean {
        return CoreClanService.delete(this)
    }

    override fun view() = ClanViewImpl(
        clanID = clanID,
        uuid = uuid,
        name = name,
        tag = tag,
        createdByUuid = createdByUuid,
        description = description,
        discordInvite = discordInvite,
        clanTagColor = clanTagColor,
        members = members.mapTo(mutableObjectSetOf()) { it.view() },
        updatedAt = updatedAt,
        createdAt = createdAt
    )

}