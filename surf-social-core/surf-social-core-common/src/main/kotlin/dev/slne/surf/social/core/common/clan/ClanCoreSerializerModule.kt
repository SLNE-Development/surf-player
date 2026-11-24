package dev.slne.surf.social.core.common.clan

import dev.slne.surf.social.api.clan.clan.Clan
import dev.slne.surf.social.api.clan.invite.ClanInvite
import dev.slne.surf.social.api.clan.member.ClanMember
import dev.slne.surf.social.core.common.clan.clan.ClanImpl
import dev.slne.surf.social.core.common.clan.invite.ClanInviteImpl
import dev.slne.surf.social.core.common.clan.member.ClanMemberImpl
import kotlinx.serialization.modules.SerializersModule

object ClanCoreSerializerModule {
    val module = SerializersModule {
        polymorphic(ClanInvite::class, ClanInviteImpl::class, ClanInviteImpl.serializer())
        polymorphic(ClanMember::class, ClanMemberImpl::class, ClanMemberImpl.serializer())
        polymorphic(Clan::class, ClanImpl::class, ClanImpl.serializer())
    }
}