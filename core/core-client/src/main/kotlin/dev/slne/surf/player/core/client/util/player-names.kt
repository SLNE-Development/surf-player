package dev.slne.surf.player.core.client.util

import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.asLoadingCache
import com.sksamuel.aedile.core.expireAfterWrite
import dev.slne.surf.api.core.service.PlayerLookupService
import dev.slne.surf.core.api.common.SurfCoreApi
import kotlinx.coroutines.CancellationException
import java.util.*
import kotlin.time.Duration.Companion.minutes

/*
 * Player names are resolved from the network's own records first and from Mojang last:
 *
 * 1. the players online in the network (surf-core),
 * 2. surf-core's player records (`surf_players`, the last name a player joined with),
 * 3. Mojang (PlayerLookupService).
 *
 * Mojang only knows premium accounts, so it cannot name players of an offline-mode network (dev and
 * test networks, whose players have offline uuids); surf-core has a record of every player that ever
 * joined, whatever their uuid. Lookups past the online players are cached for two minutes.
 */

private val usernameSources: List<suspend (UUID) -> String?> = listOf(
    { uuid -> SurfCoreApi.getOfflinePlayer(uuid)?.lastKnownName },
    { uuid -> PlayerLookupService.getUsername(uuid) },
)

private val uuidSources: List<suspend (String) -> UUID?> = listOf(
    { name -> SurfCoreApi.getOfflinePlayer(name)?.uuid },
    { name -> PlayerLookupService.getUuid(name) },
)

private val offlineUsernames = Caffeine.newBuilder()
    .maximumSize(10_000)
    .expireAfterWrite(2.minutes)
    .asLoadingCache<UUID, String?> { uuid -> firstResolved(uuid, usernameSources) }

private val offlineUuids = Caffeine.newBuilder()
    .maximumSize(10_000)
    .expireAfterWrite(2.minutes)
    .asLoadingCache<String, UUID?> { name -> firstResolved(name, uuidSources) }

/**
 * The name of the player identified by [uuid], or `null` if no source knows it.
 */
suspend fun resolvePlayerName(uuid: UUID): String? =
    SurfCoreApi.getPlayer(uuid)?.lastKnownName ?: offlineUsernames.get(uuid)

/**
 * The uuid of the player named [name] (ignoring case where the source does), or `null` if no source
 * knows them.
 */
suspend fun resolvePlayerUuid(name: String): UUID? =
    SurfCoreApi.getPlayer(name)?.uuid ?: offlineUuids.get(name)

/**
 * The name of the player identified by [uuid], falling back to the uuid itself where the name is
 * unknown.
 */
suspend fun usernameOrUuid(uuid: UUID): String = resolvePlayerName(uuid) ?: uuid.toString()

/**
 * The first non-null answer of [sources] for [key], asked in order. A source that fails is skipped,
 * so an unreachable Mojang API or surf-core never hides what another source knows.
 */
internal suspend fun <K : Any, V : Any> firstResolved(key: K, sources: List<suspend (K) -> V?>): V? {
    for (source in sources) {
        val value = try {
            source(key)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
        if (value != null) return value
    }
    return null
}
