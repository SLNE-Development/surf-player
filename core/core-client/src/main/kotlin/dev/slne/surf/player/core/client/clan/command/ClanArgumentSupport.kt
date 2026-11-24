package dev.slne.surf.player.core.client.clan.command

import dev.slne.surf.player.api.clan.SurfClanApi
import dev.slne.surf.player.api.clan.invite.ClanInvite
import dev.slne.surf.player.api.clan.member.ClanMember
import dev.slne.surf.player.core.client.util.resolvePlayerUuid
import dev.slne.surf.player.core.client.util.usernameOrUuid
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.util.*
import java.util.concurrent.ConcurrentHashMap

private const val LOOKUP_CONCURRENCY = 64

/**
 * The tags of the clans [player] currently has a pending invitation to.
 */
suspend fun invitedClanTags(player: UUID): Set<String> {
    val invites = SurfClanApi.findClanInvites(player)

    if (invites.isEmpty()) return emptySet()

    val clanTags = ConcurrentHashMap.newKeySet<String>()
    val semaphore = Semaphore(LOOKUP_CONCURRENCY)
    supervisorScope {
        for (invite in invites) {
            launch {
                semaphore.withPermit {
                    invite.getClan()?.let { clanTags.add(it.tag) }
                }
            }
        }
    }

    return clanTags
}

/**
 * The pending invitation of [player] to the clan whose tag or name is [input], ignoring case.
 */
suspend fun findClanInviteByTagOrName(player: UUID, input: String): ClanInvite? {
    val invites = SurfClanApi.findClanInvites(player)

    return coroutineScope {
        invites.map { invite -> async { invite to invite.getClan() } }
            .awaitAll()
            .firstOrNull { (_, clan) ->
                clan != null && (clan.tag.equals(input, ignoreCase = true) || clan.name.equals(input, ignoreCase = true))
            }
            ?.first
    }
}

/**
 * The names of the members of [player]'s clan, falling back to a member's uuid where their name is
 * unknown.
 */
suspend fun clanMemberNames(player: UUID): Set<String> {
    val clan = SurfClanApi.findClanByPlayer(player) ?: return emptySet()

    val memberNames = ConcurrentHashMap.newKeySet<String>()
    val semaphore = Semaphore(LOOKUP_CONCURRENCY)
    supervisorScope {
        for (member in clan.members) {
            launch {
                semaphore.withPermit {
                    memberNames.add(usernameOrUuid(member.uuid))
                }
            }
        }
    }

    return memberNames
}

/**
 * The member of [player]'s clan named [nameOrUuid] (name ignoring case, or uuid).
 */
suspend fun findClanMember(player: UUID, nameOrUuid: String): ClanMember? {
    val clan = SurfClanApi.findClanByPlayer(player) ?: return null
    val uuid = runCatching { UUID.fromString(nameOrUuid) }.getOrNull()

    if (uuid != null) {
        return clan.getMember(uuid)
    }

    val members = coroutineScope {
        clan.members.map { member -> async { member to usernameOrUuid(member.uuid) } }.awaitAll()
    }

    return findByName(members, nameOrUuid)
        ?: resolvePlayerUuid(nameOrUuid)?.let(clan::getMember)
}

/**
 * The value of the first of [named] whose name is [input], ignoring case.
 */
internal fun <T> findByName(named: List<Pair<T, String>>, input: String): T? =
    named.firstOrNull { (_, name) -> name.equals(input, ignoreCase = true) }?.first

/**
 * The names of the players [player]'s clan has invited and who have not answered yet.
 */
suspend fun pendingInviteeNames(player: UUID): Set<String> {
    val clan = SurfClanApi.findClanByPlayer(player) ?: return emptySet()

    return coroutineScope {
        clan.getPendingInvites().map { invite -> async { usernameOrUuid(invite.invited) } }.awaitAll().toSet()
    }
}

/**
 * The pending invitation of [player]'s clan for the player named [input] (ignoring case).
 */
suspend fun findPendingInvite(player: UUID, input: String): ClanInvite? {
    val clan = SurfClanApi.findClanByPlayer(player) ?: return null
    val invites = clan.getPendingInvites()
    val uuid = runCatching { UUID.fromString(input) }.getOrNull()

    if (uuid != null) {
        return invites.firstOrNull { it.invited == uuid }
    }

    val named = coroutineScope {
        invites.map { invite -> async { invite to usernameOrUuid(invite.invited) } }.awaitAll()
    }

    return findByName(named, input)
        ?: resolvePlayerUuid(input)?.let { invitee -> invites.firstOrNull { it.invited == invitee } }
}
