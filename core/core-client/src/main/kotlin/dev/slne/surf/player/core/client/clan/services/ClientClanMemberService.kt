package dev.slne.surf.player.core.client.clan.services

import com.github.benmanes.caffeine.cache.Caffeine
import dev.slne.surf.api.core.service.PlayerLookupService
import dev.slne.surf.player.api.clan.member.ClanMember
import dev.slne.surf.player.api.clan.member.ClanMemberAddResult
import dev.slne.surf.player.api.clan.member.ClanMemberRole
import dev.slne.surf.player.api.clan.member.listener.ClanMemberChangedRoleListener
import dev.slne.surf.player.api.clan.member.listener.ClanMemberListener
import dev.slne.surf.player.core.client.clan.rpc.clanMemberRpcService
import dev.slne.surf.player.core.client.util.ListenerRegistry
import dev.slne.surf.player.core.common.clan.member.ClanMemberImpl
import java.util.*

/**
 * Clan members as seen by a client, loaded from the microservice.
 */
object ClientClanMemberService {
    private val loadedMembers = Caffeine.newBuilder()
        .weakValues()
        .maximumSize(10_000)
        .build<UUID, ClanMemberImpl>()

    private val listeners = ListenerRegistry<ClanMemberListener>()

    fun registerListener(listener: ClanMemberListener) {
        listeners.register(listener)
    }

    fun unregisterListener(listener: ClanMemberListener) {
        listeners.unregister(listener)
    }

    private fun callClanMemberChangedRoleListeners(member: ClanMember, role: ClanMemberRole) {
        listeners.notify<ClanMemberChangedRoleListener> { it.onClanMemberChangedRole(member, role) }
    }

    suspend fun findMemberByName(name: String): ClanMember? {
        val uuid = PlayerLookupService.getUuid(name) ?: return null
        return findMemberByUuid(uuid)
    }

    suspend fun findMemberByUuid(uuid: UUID): ClanMember? {
        val member = loadedMembers.getIfPresent(uuid)
        if (member != null) return member

        val loaded = clanMemberRpcService.findMemberByUUID(uuid) ?: return null
        loadedMembers.put(uuid, loaded)

        return loaded
    }

    suspend fun addMember(
        clanID: ULong,
        player: UUID,
        role: ClanMemberRole,
        invitedBy: UUID?
    ): ClanMemberAddResult {
        val result = clanMemberRpcService.createMember(clanID, player, role, invitedBy)

        if (result is ClanMemberAddResult.Success) {
            ClientClanService.invalidateCachedClanByID(clanID)
        }

        return result
    }

    suspend fun removeMember(clanID: ULong, player: UUID): Boolean {
        val result = clanMemberRpcService.deleteMember(clanID, player)

        if (result) {
            ClientClanService.invalidateCachedClanByID(clanID)
        }

        return result
    }

    suspend fun changeRole(
        member: ClanMemberImpl,
        role: ClanMemberRole
    ): Boolean {
        val changed = clanMemberRpcService.changeMemberRole(member.ID, role)

        if (changed) {
            ClientClanService.invalidateCachedClanByMember(member.uuid)
            member.role = role
            loadedMembers.getIfPresent(member.uuid)?.let {
                it.role = role
            }

            callClanMemberChangedRoleListeners(member, role)
        }

        return changed
    }
}
