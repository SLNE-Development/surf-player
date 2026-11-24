package dev.slne.surf.player.core.common.clan

import dev.slne.surf.player.api.clan.clan.Clan
import dev.slne.surf.player.api.clan.invite.ClanInvite
import dev.slne.surf.player.api.clan.member.ClanMember
import dev.slne.surf.player.core.common.clan.clan.ClanImpl
import dev.slne.surf.player.core.common.clan.invite.ClanInviteImpl
import dev.slne.surf.player.core.common.clan.member.ClanMemberImpl
import kotlinx.serialization.modules.SerializersModule

object ClanCoreSerializerModule {
    val module = SerializersModule {
        polymorphic(ClanInvite::class, ClanInviteImpl::class, ClanInviteImpl.serializer())
        polymorphic(ClanMember::class, ClanMemberImpl::class, ClanMemberImpl.serializer())
        polymorphic(Clan::class, ClanImpl::class, ClanImpl.serializer())
    }
}