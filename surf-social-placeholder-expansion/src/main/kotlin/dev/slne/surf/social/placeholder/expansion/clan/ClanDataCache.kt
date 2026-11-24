package dev.slne.surf.social.placeholder.expansion.clan

import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.asLoadingCache
import com.sksamuel.aedile.core.expireAfterWrite
import com.sksamuel.aedile.core.refreshAfterWrite
import dev.slne.surf.social.api.clan.clan.Clan
import dev.slne.surf.social.api.clan.clan.listener.ClanUpdatedListener
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
        val clan = Clan.byPlayer(uuid) ?: return CachedClanData.Empty
        return clan.toCachedData()
    }

    fun getData(uuid: UUID): CachedClanData {
        return cache.underlying().get(uuid).getNow(CachedClanData.Empty)
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
