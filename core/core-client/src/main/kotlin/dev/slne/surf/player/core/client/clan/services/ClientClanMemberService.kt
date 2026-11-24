package dev.slne.surf.player.core.client.clan.services

import dev.slne.surf.player.api.clan.member.ClanMember
import dev.slne.surf.player.api.clan.member.ClanMemberAddResult
import dev.slne.surf.player.api.clan.member.ClanMemberRole
import dev.slne.surf.player.api.clan.member.listener.ClanMemberChangedRoleListener
import dev.slne.surf.player.api.clan.member.listener.ClanMemberListener
import dev.slne.surf.player.core.client.clan.rpc.clanMemberRpcService
import dev.slne.surf.player.core.client.util.ListenerRegistry
import dev.slne.surf.player.core.client.util.resolvePlayerUuid
import dev.slne.surf.player.core.common.clan.member.ClanMemberImpl
import java.util.*

/**
 * Clan members as seen by a client, loaded from the microservice.
 */
object ClientClanMemberService {
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
        val uuid = resolvePlayerUuid(name) ?: return null
        return findMemberByUuid(uuid)
    }

    /**
     * The membership of [uuid], read from their clan in the clan cache. That cache is shared through
     * Redis and invalidated by every membership change (join, leave, kick, role change, deletion),
     * so no server keeps a stale membership around.
     */
    suspend fun findMemberByUuid(uuid: UUID): ClanMember? =
        ClientClanService.findClanByPlayer(uuid)?.getMember(uuid)

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
            ClientClanService.invalidateCachedClanByMember(player)
        }

        return result
    }

    suspend fun changeRole(
        member: ClanMemberImpl,
        role: ClanMemberRole
    ): Boolean {
        val changed = clanMemberRpcService.updateMemberRole(member.ID, role)

        if (changed) {
            ClientClanService.invalidateCachedClanByMember(member.uuid)
            member.role = role

            callClanMemberChangedRoleListeners(member, role)
        }

        return changed
    }
}
