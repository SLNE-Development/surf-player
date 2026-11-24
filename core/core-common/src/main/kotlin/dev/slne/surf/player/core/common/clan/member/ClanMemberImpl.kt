package dev.slne.surf.player.core.common.clan.member

import dev.slne.surf.player.api.clan.member.ClanMember
import dev.slne.surf.player.api.clan.member.ClanMemberRole
import dev.slne.surf.player.core.common.clan.CoreClanApi
import dev.slne.surf.api.core.serializer.java.datetime.datetime.offset.SerializableOffsetDateTime
import dev.slne.surf.api.core.serializer.java.uuid.SerializableStringUUID
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

@Serializable
data class ClanMemberImpl(
    val ID: ULong,
    override val uuid: SerializableStringUUID,
    @field:Volatile override var role: ClanMemberRole,
    override val addedBy: SerializableStringUUID?,
    val createdAt: SerializableOffsetDateTime = OffsetDateTime.now(),
    val updatedAt: SerializableOffsetDateTime? = OffsetDateTime.now(),
    override val lastActiveAt: SerializableOffsetDateTime = OffsetDateTime.now()
) : AbstractClanMemberView(), ClanMember {
    override suspend fun changeRole(role: ClanMemberRole): Boolean {
        return CoreClanApi.changeRole(this, role)
    }

    override fun view() = ClanMemberViewImpl(
        uuid = uuid,
        role = role,
        addedBy = addedBy,
        lastActiveAt = lastActiveAt
    )
}