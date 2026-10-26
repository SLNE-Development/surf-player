package dev.slne.surf.minimapcompanion.core.tracker

import dev.slne.surf.api.core.util.logger
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
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
 * Caches what [sources] return for the players [add]ed, so the tracker can check relations every tick
 * without waiting on them. Sources also in [previous] keep its cached relations.
 */
class TrackerRelations(
    private val sources: List<RelationSource>,
    previous: TrackerRelations? = null,
    private val warn: (message: String, cause: Throwable) -> Unit = { message, cause ->
        log.atWarning().withCause(cause).log("%s", message)
    },
) {
    private val players: MutableSet<UUID> = previous?.players ?: ConcurrentHashMap.newKeySet()
    private val cachesBySource: List<MutableMap<UUID, Set<UUID>>> =
        sources.map { source -> previous?.cacheOf(source) ?: ConcurrentHashMap() }

    private fun cacheOf(source: RelationSource): MutableMap<UUID, Set<UUID>>? =
        cachesBySource.getOrNull(sources.indexOf(source))

    fun add(player: UUID) {
        players += player
    }

    /**
     * Asks every source again for [players]. A source that fails keeps its previous answer.
     */
    suspend fun refresh(players: Collection<UUID>) {
        for ((source, cache) in sources.zip(cachesBySource)) {
            var failures = 0
            var firstFailure: Throwable? = null

            for (player in players) {
                if (player !in this.players) continue

                val related = try {
                    source.related(player)
                } catch (e: CancellationException) {
                    // A source may time out its own lookup; only stop when the refresh itself is cancelled.
                    currentCoroutineContext().ensureActive()
                    failures++
                    firstFailure = firstFailure ?: e
                    continue
                } catch (e: Exception) {
                    failures++
                    firstFailure = firstFailure ?: e
                    continue
                } catch (e: LinkageError) {
                    // The other plugin's API changed under the companion.
                    failures++
                    firstFailure = firstFailure ?: e
                    continue
                }

                cache[player] = related
                // forget() can run during the lookup and must win, or the player stays cached for good.
                if (player !in this.players) cache.remove(player)
            }

            firstFailure?.let {
                warn("Failed to load the tracker relations of $failures player(s) from ${source.javaClass.simpleName}", it)
            }
        }
    }

    fun isRelated(viewer: UUID, target: UUID) = cachesBySource.any { target in it[viewer].orEmpty() }

    fun forget(player: UUID) {
        players -= player
        for (cache in cachesBySource) {
            cache.remove(player)
        }
    }

    private companion object {
        val log = logger()
    }
}
