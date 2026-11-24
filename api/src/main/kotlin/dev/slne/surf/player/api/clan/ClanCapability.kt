package dev.slne.surf.player.api.clan

import dev.slne.surf.player.api.clan.clan.Clan
import dev.slne.surf.player.api.clan.clan.ClanCreateBuilder
import dev.slne.surf.player.api.clan.clan.ClanCreationResult
import dev.slne.surf.player.api.clan.invite.ClanInvite
import dev.slne.surf.player.api.clan.member.ClanMember
import java.util.*

/** What a single player has and can do in the clan feature. See [SurfClanApi]. */
interface ClanCapability {
    val uuid: UUID

    /** The clan this player is a member of, or `null`. */
    suspend fun clan(): Clan? = SurfClanApi.findClanByPlayer(uuid)

    /** The clan membership of this player, or `null`. */
    suspend fun clanMember(): ClanMember? = SurfClanApi.findClanMember(uuid)

    /** The pending clan invites this player received. */
    suspend fun clanInvites(): List<ClanInvite> = SurfClanApi.findClanInvites(uuid)

    /** The pending invite of the clan called [clanName] to this player, or `null`. */
    suspend fun clanInvite(clanName: String): ClanInvite? =
        SurfClanApi.findClanInvite(uuid, clanName)

    /** Creates a clan called [name] with the tag [tag], owned by this player. */
    suspend fun createClan(
        name: String,
        tag: String,
        additional: ClanCreateBuilder.() -> Unit = {}
    ): ClanCreationResult = SurfClanApi.createClan(uuid, name, tag, additional)
}
