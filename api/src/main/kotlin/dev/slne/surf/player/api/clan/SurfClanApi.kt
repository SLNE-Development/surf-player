package dev.slne.surf.player.api.clan

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.player.api.clan.clan.Clan
import dev.slne.surf.player.api.clan.clan.ClanCreateBuilder
import dev.slne.surf.player.api.clan.clan.ClanCreationResult
import dev.slne.surf.player.api.clan.clan.ClanValidationResult
import dev.slne.surf.player.api.clan.clan.listener.ClanListener
import dev.slne.surf.player.api.clan.invite.ClanInvite
import dev.slne.surf.player.api.clan.member.ClanMember
import dev.slne.surf.player.api.clan.member.listener.ClanMemberListener
import java.util.*

private val instance = requiredService<SurfClanApi>()

/**
 * Entry point to clans, their members and invites.
 *
 * Everything about a single player is also available through [ClanCapability].
 */
interface SurfClanApi {

    /** Registers [listener] to receive clan events. */
    fun registerListener(listener: ClanListener)

    /** Unregisters a [listener] registered with [registerListener]. */
    fun unregisterListener(listener: ClanListener)

    /** Registers [listener] to receive clan member events. */
    fun registerListener(listener: ClanMemberListener)

    /** Unregisters a [listener] registered with [registerListener]. */
    fun unregisterListener(listener: ClanMemberListener)

    /** The clan the player identified by [uuid] is a member of, or `null`. */
    suspend fun findClanByPlayer(uuid: UUID): Clan?

    /** The clan with the unique id [clanUuid], or `null`. */
    suspend fun findClanByUuid(clanUuid: UUID): Clan?

    /** The clan with the tag [tag], or `null`. */
    suspend fun findClanByTag(tag: String): Clan?

    /** Validates a clan [name] and [tag] against the rules a new clan must follow. */
    fun validateClanNameAndTag(name: String, tag: String): ClanValidationResult

    /**
     * Creates a clan called [name] with the tag [tag], owned by [owner].
     *
     * ```kotlin
     * val result = SurfClanApi.createClan("Warriors", "WAR", ownerUuid) {
     *     description("A clan for warriors")
     * }
     * ```
     */
    suspend fun createClan(
        owner: UUID,
        name: String,
        tag: String,
        additional: ClanCreateBuilder.() -> Unit = {}
    ): ClanCreationResult

    /** The clan membership of the player identified by [uuid], or `null`. */
    suspend fun findClanMember(uuid: UUID): ClanMember?

    /** The clan membership of the player called [name], or `null`. */
    suspend fun findClanMemberByName(name: String): ClanMember?

    /** The pending clan invites the player identified by [uuid] received. */
    suspend fun findClanInvites(uuid: UUID): List<ClanInvite>

    /** The pending invite of the clan called [clanName] to the player identified by [uuid]. */
    suspend fun findClanInvite(uuid: UUID, clanName: String): ClanInvite?

    /** Whether the player identified by [uuid] accepts clan invites. */
    suspend fun acceptsClanInvites(uuid: UUID): Boolean

    /**
     * Changes whether the player identified by [uuid] accepts clan invites.
     *
     * @return `true` if the preference was changed
     */
    suspend fun setAcceptsClanInvites(uuid: UUID, acceptsClanInvites: Boolean): Boolean

    companion object : SurfClanApi by instance {
        val INSTANCE: SurfClanApi get() = instance
    }
}
