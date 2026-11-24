package dev.slne.surf.player.placeholder.expansion.clan

import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.asLoadingCache
import com.sksamuel.aedile.core.expireAfterWrite
import com.sksamuel.aedile.core.refreshAfterWrite
import dev.slne.surf.player.api.clan.SurfClanApi
import dev.slne.surf.player.api.clan.clan.Clan
import dev.slne.surf.player.api.clan.clan.listener.ClanUpdatedListener
import java.util.*
import java.util.concurrent.CompletableFuture.completedFuture
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/** Active members a clan needs before its tag is shown. */
private const val MIN_ACTIVE_MEMBERS_FOR_TAG = 30

object ClanDataCache : ClanUpdatedListener {
    private val cache = Caffeine.newBuilder()
        .refreshAfterWrite(30.seconds)
        .expireAfterWrite(10.minutes)
        .maximumSize(30_000)
        .asLoadingCache<UUID, CachedClanData> { uuid ->
            loadClanData(uuid)
        }

    private suspend fun loadClanData(uuid: UUID): CachedClanData {
        val clan = SurfClanApi.findClanByPlayer(uuid) ?: return CachedClanData.Empty
        return clan.toCachedData()
    }

    /**
     * The cached clan data of [uuid]. Never waits: until the data is loaded (started by the first
     * call) the player counts as clanless, so a server calls [preload] before the first render.
     */
    fun getData(uuid: UUID): CachedClanData {
        return cache.underlying().get(uuid).getNow(CachedClanData.Empty)
    }

    /**
     * Loads the clan data of [uuid] into the cache, e.g. while the player is connecting, so the first
     * placeholder rendered for them (a join message) already shows their clan.
     */
    suspend fun preload(uuid: UUID) {
        cache.get(uuid)
    }

    override fun onClanUpdated(clan: Clan) {
        val updated = clan.toCachedData()
        val entries = cache.underlying().asMap()

        for (member in clan.members) {
            entries.computeIfPresent(member.uuid) { _, _ -> completedFuture(updated) }
        }
    }

    private fun Clan.toCachedData() =
        CachedClanData.Loaded(name, tag, renderClanTag(minSize = MIN_ACTIVE_MEMBERS_FOR_TAG))
}
