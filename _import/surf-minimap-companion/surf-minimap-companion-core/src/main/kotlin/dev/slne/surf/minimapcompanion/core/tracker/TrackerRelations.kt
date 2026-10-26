package dev.slne.surf.minimapcompanion.core.tracker

import dev.slne.surf.api.core.util.logger
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.cancellation.CancellationException

/**
 * Tells which players a player is related to, such as their friends. Relations must be symmetric.
 */
fun interface RelationSource {
    suspend fun related(player: UUID): Set<UUID>
}

/**
 * Caches what [sources] return, so the tracker can check relations every tick without waiting on them.
 */
class TrackerRelations(private val sources: List<RelationSource>) {
    private val cachesBySource = sources.map { ConcurrentHashMap<UUID, Set<UUID>>() }

    /**
     * Asks every source again for [players]. A source that fails keeps its previous answer.
     */
    suspend fun refresh(players: Collection<UUID>) {
        for (player in players) {
            for ((source, cache) in sources.zip(cachesBySource)) {
                try {
                    cache[player] = source.related(player)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    log.atWarning().withCause(e).log("Failed to load the tracker relations of %s", player)
                }
            }
        }
    }

    fun isRelated(viewer: UUID, target: UUID) = cachesBySource.any { target in it[viewer].orEmpty() }

    fun forget(player: UUID) {
        for (cache in cachesBySource) {
            cache.remove(player)
        }
    }

    private companion object {
        val log = logger()
    }
}
