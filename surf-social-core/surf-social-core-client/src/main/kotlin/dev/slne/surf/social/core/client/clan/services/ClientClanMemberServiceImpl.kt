package dev.slne.surf.social.core.client.clan.services

import com.github.benmanes.caffeine.cache.Caffeine
import com.google.auto.service.AutoService
import dev.slne.surf.social.api.clan.member.ClanMember
import dev.slne.surf.social.api.clan.member.ClanMemberAddResult
import dev.slne.surf.social.api.clan.member.ClanMemberRole
import dev.slne.surf.social.api.clan.member.ClanMemberService
import dev.slne.surf.social.api.clan.member.listener.ClanMemberChangedRoleListener
import dev.slne.surf.social.api.clan.member.listener.ClanMemberListener
import dev.slne.surf.api.core.service.PlayerLookupService
import dev.slne.surf.social.core.client.clan.rpc.clanMemberRpcService
import dev.slne.surf.social.core.client.util.ListenerRegistry
import dev.slne.surf.social.core.common.clan.member.ClanMemberImpl
import dev.slne.surf.social.core.common.clan.member.CoreClanMemberService
import java.util.*

@AutoService(ClanMemberService::class)
class ClientClanMemberServiceImpl : CoreClanMemberService {
    private val loadedMembers = Caffeine.newBuilder()
        .weakValues()
        .maximumSize(10_000)
        .build<UUID, ClanMemberImpl>()

    private val listeners = ListenerRegistry<ClanMemberListener>()

    override fun registerListener(listener: ClanMemberListener) {
        listeners.register(listener)
    }

    override fun unregisterListener(listener: ClanMemberListener) {
        listeners.unregister(listener)
    }

    private fun callClanMemberChangedRoleListeners(member: ClanMember, role: ClanMemberRole) {
        listeners.notify<ClanMemberChangedRoleListener> { it.onClanMemberChangedRole(member, role) }
    }

    override suspend fun findMemberByName(name: String): ClanMember? {
        val uuid = PlayerLookupService.getUuid(name) ?: return null
        return findMemberByUuid(uuid)
    }

    override suspend fun findMemberByUuid(uuid: UUID): ClanMember? {
        val member = loadedMembers.getIfPresent(uuid)
        if (member != null) return member

        val loaded = clanMemberRpcService.findMemberByUUID(uuid) ?: return null
        loadedMembers.put(uuid, loaded)

        return loaded
    }

    override suspend fun addMember(
        clanID: ULong,
        player: UUID,
        role: ClanMemberRole,
        invitedBy: UUID?
    ): ClanMemberAddResult {
        val result = clanMemberRpcService.createMember(clanID, player, role, invitedBy)

        if (result is ClanMemberAddResult.Success) {
            ClientClanServiceImpl.get().invalidateCachedClanByID(clanID)
        }

        return result
    }

    override suspend fun removeMember(clanID: ULong, player: UUID): Boolean {
        val result = clanMemberRpcService.deleteMember(clanID, player)

        if (result) {
            ClientClanServiceImpl.get().invalidateCachedClanByID(clanID)
        }

        return result
    }

    override suspend fun changeRole(
        member: ClanMemberImpl,
        role: ClanMemberRole
    ): Boolean {
        val changed = clanMemberRpcService.changeMemberRole(member.ID, role)

        if (changed) {
            ClientClanServiceImpl.get().invalidateCachedClanByMember(member.uuid)
            member.role = role
            loadedMembers.getIfPresent(member.uuid)?.let {
                it.role = role
            }

            callClanMemberChangedRoleListeners(member, role)
        }

        return changed
    }

    companion object {
        fun get() = ClanMemberService.INSTANCE as ClientClanMemberServiceImpl
    }
}