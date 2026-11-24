package dev.slne.surf.player.core.client.clan.services

import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.asLoadingCache
import com.sksamuel.aedile.core.expireAfterWrite
import dev.slne.surf.player.api.clan.clan.*
import dev.slne.surf.player.api.clan.clan.listener.*
import dev.slne.surf.player.api.clan.invite.ClanInviteResult
import dev.slne.surf.player.api.clan.member.ClanMemberAddResult
import dev.slne.surf.player.api.clan.member.ClanMemberRole
import dev.slne.surf.player.core.client.SurfPlayerClient
import dev.slne.surf.player.core.client.clan.redis.ClanCaches
import dev.slne.surf.player.core.client.clan.rpc.clanRpcService
import dev.slne.surf.player.core.client.util.ListenerRegistry
import dev.slne.surf.player.core.common.clan.clan.AbstractClanView
import dev.slne.surf.player.core.common.clan.clan.ClanImpl
import dev.slne.surf.player.core.common.clan.clan.ClanTagRules
import dev.slne.surf.player.core.common.clan.invite.ClanInviteImpl
import dev.slne.surf.player.core.common.clan.member.ClanMemberImpl
import it.unimi.dsi.fastutil.chars.Char2BooleanOpenHashMap
import java.util.*
import kotlin.time.Duration.Companion.minutes

/**
 * Clans as seen by a client: cached in Redis and loaded from the microservice.
 */
object ClientClanService {
    private val cache = SurfPlayerClient.redisApi.createSimpleSetRedisCache(
        ClanCaches.key("clans"),
        ClanImpl.serializer(),
        15.minutes,
        { it.clanID.toString() },
        ClanCacheIndexes
    )

    private val tagSuggestionBucketCache = Caffeine.newBuilder()
        .maximumSize(5_000)
        .expireAfterWrite(1.minutes)
        .asLoadingCache<String, List<String>> { bucketKey ->
            clanRpcService.findClanTagsByPrefix(bucketKey, 100)
        }

    private val listeners = ListenerRegistry<ClanListener>()

    /** Creates the clan caches. Must run before the Redis connection is established. */
    fun init() = Unit

    fun registerListener(listener: ClanListener) {
        listeners.register(listener)
    }

    fun unregisterListener(listener: ClanListener) {
        listeners.unregister(listener)
    }

    private fun callClanCreatedListeners(clan: Clan) {
        listeners.notify<ClanCreatedListener> { it.onClanCreated(clan) }
    }

    private fun callClanDeletedListeners(clan: Clan) {
        val view = clan.view()
        listeners.notify<ClanDeletedListener> { it.onClanDeleted(view) }
    }

    private fun callClanUpdatedListeners(clan: Clan) {
        listeners.notify<ClanUpdatedListener> { it.onClanUpdated(clan) }
    }

    private fun callClanMemberUpdatedListeners(clan: Clan, memberUuid: UUID, added: Boolean) {
        listeners.notify<ClanUpdateMemberListener> { it.onClanMemberUpdated(clan, memberUuid, added) }
    }

    suspend fun invalidateCaches() {
        cache.invalidateAll()
        invalidateTagSuggestions()
    }

    /**
     * Drops the cached tag suggestions after a clan was created or deleted on this server, so its
     * tag is (no longer) suggested at once here; other servers catch up within a minute.
     */
    private fun invalidateTagSuggestions() {
        tagSuggestionBucketCache.underlying().synchronous().invalidateAll()
    }

    suspend fun invalidateCachedClanByMember(member: UUID) {
        cache.removeByIndex(ClanCacheIndexes.members, member)
    }

    suspend fun invalidateCachedClanByID(id: ULong) {
        cache.removeById(id.toString())
    }

    suspend fun findClanByPlayer(playerUuid: UUID): Clan? {
        return cache.findCachedByIndexOrLoadNullable(ClanCacheIndexes.members, playerUuid) {
            clanRpcService.findClanByMember(playerUuid)
        }
    }

    suspend fun findClanByUuid(clanUuid: UUID): Clan? {
        return cache.findCachedByIndexOrLoadNullable(ClanCacheIndexes.uuid, clanUuid) {
            clanRpcService.findClanByUuid(clanUuid)
        }
    }

    suspend fun findClanByTag(tag: String): Clan? {
        return cache.findCachedByIndexOrLoadNullable(ClanCacheIndexes.tag, tag) {
            clanRpcService.findClanByTag(normalizeTag(tag))
        }
    }

    suspend fun findClanByID(id: ULong): ClanImpl? {
        return cache.findCachedByIndexOrLoadNullable(ClanCacheIndexes.id, id) {
            clanRpcService.findClanById(id)
        }
    }

    suspend fun fetchAllClansWithoutMembersSortByMemberCount(): Collection<ClanImpl> {
        return clanRpcService.findClansByMemberCount()
    }

    fun validateClanNameAndTag(
        name: String,
        tag: String
    ): ClanValidationResult {
        if (name.length < Clan.MIN_NAME_LENGTH || name.length > Clan.MAX_NAME_LENGTH) {
            return ClanValidationResult.NameOutOfRange(Clan.MIN_NAME_LENGTH, Clan.MAX_NAME_LENGTH)
        }

        if (tag.length < Clan.MIN_TAG_LENGTH || tag.length > Clan.MAX_TAG_LENGTH) {
            return ClanValidationResult.TagOutOfRange(Clan.MIN_TAG_LENGTH, Clan.MAX_TAG_LENGTH)
        }

        val charValidations = Char2BooleanOpenHashMap(tag.length)
        var allCharsValid = true
        for (char in tag) {
            val valid = char.isLetterOrDigit()
            charValidations[char] = valid
            allCharsValid = allCharsValid && valid
        }
        if (!allCharsValid) {
            return ClanValidationResult.InvalidTagCharacters(charValidations)
        }

        if (!ClanTagRules.isValid(tag)) {
            return ClanValidationResult.TagViolation
        }

        return ClanValidationResult.Valid
    }

    suspend fun createClan(properties: ClanCreateBuilder): ClanCreationResult {
        val nameTagValidation = validateClanNameAndTag(properties.name, properties.tag)
        if (nameTagValidation != ClanValidationResult.Valid) {
            return ClanCreationResult.InvalidTagOrName(nameTagValidation)
        }

        val result = clanRpcService.createClan(
            properties.name,
            normalizeTag(properties.tag),
            properties.owner,
            properties.tagColor?.foregroundColor,
            properties.tagColor?.backgroundColor,
            properties.tagColor?.shadowColor,
            properties.description,
            properties.discordInvite
        )

        if (result is ClanCreationResult.Success) {
            invalidateTagSuggestions()
            callClanCreatedListeners(result.clan)
        }

        return result
    }

    suspend fun updateDescription(
        clan: ClanImpl,
        description: String?
    ): Boolean {
        val updated = clanRpcService.updateClanDescription(clan.clanID, description)

        if (updated) {
            invalidateCachedClanByID(clan.clanID)
            clan.description = description
            callClanUpdatedListeners(clan)
        }

        return updated
    }

    suspend fun updateDiscordInvite(
        clan: ClanImpl,
        discordInvite: String?
    ): Boolean {
        val updated = clanRpcService.updateClanDiscordInvite(clan.clanID, discordInvite)

        if (updated) {
            invalidateCachedClanByID(clan.clanID)
            clan.discordInvite = discordInvite
            callClanUpdatedListeners(clan)
        }

        return updated
    }

    suspend fun updateTagColor(
        clan: ClanImpl,
        update: ClanTagColorUpdate
    ): Boolean {
        val updatedTagColor = clan.clanTagColor.update(update)
        val updated = clanRpcService.updateClanTagColor(
            clan.clanID,
            updatedTagColor.foregroundColor,
            updatedTagColor.backgroundColor,
            updatedTagColor.shadowColor
        )

        if (updated) {
            invalidateCachedClanByID(clan.clanID)
            clan.clanTagColor = updatedTagColor
            callClanUpdatedListeners(clan)
        }

        return updated
    }

    suspend fun fetchPendingInvites(clan: AbstractClanView): Set<ClanInviteImpl> {
        return ClientClanInviteService.fetchPendingInvites(clan.clanID)
    }

    suspend fun invitePlayer(
        clan: ClanImpl,
        invitee: UUID,
        invitedBy: UUID
    ): ClanInviteResult {
        return ClientClanInviteService.createInvite(clan.clanID, invitee, invitedBy)
    }

    suspend fun revokeInvite(
        clan: ClanImpl,
        playerUuid: UUID
    ): Boolean {
        return ClientClanInviteService.deleteInvite(clan.clanID, playerUuid)
    }

    suspend fun addMember(
        clan: ClanImpl,
        playerUuid: UUID,
        role: ClanMemberRole,
        addedBy: UUID?
    ): ClanMemberAddResult {
        val result = ClientClanMemberService.addMember(clan.clanID, playerUuid, role, addedBy)
        if (result is ClanMemberAddResult.Success) {
            clan.addMemberLocally(result.member as ClanMemberImpl)
            callClanMemberUpdatedListeners(clan, result.member.uuid, true)
        }

        return result
    }

    suspend fun removeMember(
        clan: ClanImpl,
        playerUuid: UUID
    ): Boolean {
        val result = ClientClanMemberService.removeMember(clan.clanID, playerUuid)
        if (result) {
            clan.removeMemberLocally(playerUuid)
            callClanMemberUpdatedListeners(clan, playerUuid, false)
        }

        return result
    }

    suspend fun delete(clan: ClanImpl): Boolean {
        val deleted = clanRpcService.deleteClan(clan.clanID)

        if (deleted) {
            invalidateCachedClanByID(clan.clanID)
            invalidateTagSuggestions()
            callClanDeletedListeners(clan)
        }

        return deleted
    }

    suspend fun computeTagSuggestions(
        input: String,
        limit: Int = 100
    ): Collection<String> {
        val normalized = normalizeTag(input)
        val bucketTags = tagSuggestionBucketCache.get(tagSuggestionBucketKey(normalized))

        return filterTagSuggestions(bucketTags, normalized, limit)
    }

    private fun normalizeTag(tag: String) = ClanTagRules.normalize(tag)
}

/**
 * The key of the cached tag list a normalized input is answered from: its first three characters.
 * An empty input has its own bucket (the first tags of all clans), so a tag is suggested before
 * anything was typed.
 */
internal fun tagSuggestionBucketKey(normalizedInput: String): String = normalizedInput.take(3)

/** The tags of [bucketTags] that start with [normalizedInput], at most [limit] (1..100). */
internal fun filterTagSuggestions(bucketTags: List<String>, normalizedInput: String, limit: Int): List<String> =
    bucketTags.asSequence()
        .filter { it.startsWith(normalizedInput) }
        .take(limit.coerceIn(1, 100))
        .toList()
