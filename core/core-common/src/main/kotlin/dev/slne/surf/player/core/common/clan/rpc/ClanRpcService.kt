package dev.slne.surf.player.core.common.clan.rpc

import dev.slne.surf.player.api.clan.clan.ClanCreationResult
import dev.slne.surf.player.core.common.clan.clan.ClanImpl
import dev.slne.surf.rabbitmq.api.rpc.RpcService
import net.kyori.adventure.text.format.ShadowColor
import net.kyori.adventure.text.format.TextColor
import java.util.*

/** Clans, stored by the microservice. The `update*` and `delete*` methods return `false` if the clan does not exist. */
@RpcService
interface ClanRpcService {

    suspend fun createClan(
        name: String,
        tag: String,
        owner: UUID,
        tagForegroundColor: TextColor?,
        tagBackgroundColor: TextColor?,
        tagShadowColor: ShadowColor?,
        description: String?,
        discordInvite: String?
    ): ClanCreationResult

    suspend fun deleteClan(clanId: ULong): Boolean

    /** Every clan without its member list, the clan with the most members first. */
    suspend fun findClansByMemberCount(): List<ClanImpl>
    suspend fun findClanById(clanId: ULong): ClanImpl?
    suspend fun findClanByMember(memberUuid: UUID): ClanImpl?
    suspend fun findClanByTag(tag: String): ClanImpl?
    suspend fun findClanByUuid(clanUuid: UUID): ClanImpl?

    /** Up to [limit] tags starting with [prefix]. */
    suspend fun findClanTagsByPrefix(prefix: String, limit: Int): List<String>

    suspend fun updateClanDescription(clanId: ULong, description: String?): Boolean
    suspend fun updateClanDiscordInvite(clanId: ULong, discordInvite: String?): Boolean
    suspend fun updateClanTagColor(
        clanId: ULong,
        tagForegroundColor: TextColor?,
        tagBackgroundColor: TextColor?,
        tagShadowColor: ShadowColor?
    ): Boolean
}
