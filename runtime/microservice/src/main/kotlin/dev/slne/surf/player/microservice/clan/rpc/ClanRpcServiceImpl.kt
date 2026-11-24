package dev.slne.surf.player.microservice.clan.rpc

import dev.slne.surf.player.api.clan.clan.ClanCreationResult
import dev.slne.surf.player.core.common.clan.clan.ClanImpl
import dev.slne.surf.player.core.common.clan.rpc.ClanRpcService
import dev.slne.surf.player.microservice.clan.db.repository.ClanRepository
import net.kyori.adventure.text.format.ShadowColor
import net.kyori.adventure.text.format.TextColor
import java.util.*

object ClanRpcServiceImpl : ClanRpcService {
    override suspend fun createClan(
        name: String,
        tag: String,
        owner: UUID,
        tagForegroundColor: TextColor?,
        tagBackgroundColor: TextColor?,
        tagShadowColor: ShadowColor?,
        description: String?,
        discordInvite: String?
    ): ClanCreationResult {
        return ClanRepository.create(
            name = name,
            tag = tag,
            owner = owner,
            tagForegroundColor = tagForegroundColor,
            tagBackgroundColor = tagBackgroundColor,
            tagShadowColor = tagShadowColor,
            description = description,
            discordInvite = discordInvite
        )
    }

    override suspend fun deleteClan(clanId: ULong): Boolean {
        return ClanRepository.delete(clanId)
    }

    override suspend fun findClansByMemberCount(): List<ClanImpl> {
        return ClanRepository.fetchAllClansWithoutMembersSortByMemberCount().toList()
    }

    override suspend fun findClanById(clanId: ULong): ClanImpl? {
        return ClanRepository.findClanByID(clanId)
    }

    override suspend fun findClanByMember(memberUuid: UUID): ClanImpl? {
        return ClanRepository.findClanByPlayer(memberUuid)
    }

    override suspend fun findClanByTag(tag: String): ClanImpl? {
        return ClanRepository.findClanByTag(tag)
    }

    override suspend fun findClanByUuid(clanUuid: UUID): ClanImpl? {
        return ClanRepository.findClanByUuid(clanUuid)
    }

    override suspend fun findClanTagsByPrefix(
        prefix: String,
        limit: Int
    ): List<String> {
        return ClanRepository.suggestTagsByPrefix(prefix, limit)
    }

    override suspend fun updateClanDescription(clanId: ULong, description: String?): Boolean {
        return ClanRepository.updateDescription(clanId, description)
    }

    override suspend fun updateClanDiscordInvite(clanId: ULong, discordInvite: String?): Boolean {
        return ClanRepository.updateDiscordInvite(clanId, discordInvite)
    }

    override suspend fun updateClanTagColor(
        clanId: ULong,
        tagForegroundColor: TextColor?,
        tagBackgroundColor: TextColor?,
        tagShadowColor: ShadowColor?
    ): Boolean {
        return ClanRepository.updateTagColor(
            clanID = clanId,
            tagForegroundColor = tagForegroundColor,
            tagBackgroundColor = tagBackgroundColor,
            tagShadowColor = tagShadowColor
        )
    }
}
