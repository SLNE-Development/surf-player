package dev.slne.surf.player.core.client.chat.service

import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.player.core.common.playerRedisKey
import dev.slne.surf.redis.RedisApi
import dev.slne.surf.redis.sync.set.SyncSet
import kotlinx.serialization.Serializable
import java.util.*

/** [spy] reads the private messages of [observed]. */
@Serializable
data class SpyEntry(val spy: SerializableUUID, val observed: SerializableUUID)

/** Where the [SpyEntry]s are kept: the network-wide synchronized set, or a plain set in tests. */
interface SpyStore {
    fun snapshot(): Set<SpyEntry>
    operator fun contains(entry: SpyEntry): Boolean
    fun add(entry: SpyEntry): Boolean
    fun remove(entry: SpyEntry): Boolean
    fun removeIf(predicate: (SpyEntry) -> Boolean): Boolean
}

/**
 * Who reads whose private messages, independent of the servers the spies and the observed players
 * are on.
 */
class SpyRegistry(private val store: SpyStore) {

    /** Whether [spy] reads the private messages of [observed]. */
    fun isSpying(spy: UUID, observed: UUID): Boolean = SpyEntry(spy, observed) in store

    /** Whether [spy] reads the private messages of anybody. */
    fun isSpyingAnyone(spy: UUID): Boolean = store.snapshot().any { it.spy == spy }

    /** Starts reading the private messages of [observed]; `false` if [spy] already does. */
    fun add(spy: UUID, observed: UUID): Boolean = store.add(SpyEntry(spy, observed))

    /** Stops reading the private messages of [observed]; `false` if [spy] did not. */
    fun remove(spy: UUID, observed: UUID): Boolean = store.remove(SpyEntry(spy, observed))

    /** Stops every spying of [spy], e.g. when they leave the network; `false` if there was none. */
    fun clear(spy: UUID): Boolean = store.removeIf { it.spy == spy }

    /**
     * The spies who see a private message from [sender] to [receiver]: everyone who reads the
     * private messages of one of both, except the two themselves.
     */
    fun spiesOf(sender: UUID, receiver: UUID?): Set<UUID> {
        val spies = HashSet<UUID>()

        for (entry in store.snapshot()) {
            if (entry.observed != sender && entry.observed != receiver) continue
            if (entry.spy == sender || entry.spy == receiver) continue

            spies += entry.spy
        }

        return spies
    }
}

/**
 * The spies of the whole network, kept in the synchronized set `surf-player:chat:spies`.
 */
object SpyService {
    @Volatile
    private var registry: SpyRegistry? = null

    private val spies: SpyRegistry
        get() = registry ?: error("SpyService.register has not been called yet")

    /** Creates the synchronized set; must run before the Redis connection is established. */
    fun register(redisApi: RedisApi) {
        val set = redisApi.createSyncSet<SpyEntry>(playerRedisKey("chat", "spies"))
        registry = SpyRegistry(SyncSetSpyStore(set))
    }

    fun isSpying(spy: UUID, observed: UUID) = spies.isSpying(spy, observed)
    fun isSpyingAnyone(spy: UUID) = spies.isSpyingAnyone(spy)
    fun addPrivateMessageSpy(spy: UUID, observed: UUID) = spies.add(spy, observed)
    fun removePrivateMessageSpy(spy: UUID, observed: UUID) = spies.remove(spy, observed)
    fun clearPrivateMessageSpies(spy: UUID) = spies.clear(spy)
    fun spiesOf(sender: UUID, receiver: UUID?) = spies.spiesOf(sender, receiver)

    private class SyncSetSpyStore(private val set: SyncSet<SpyEntry>) : SpyStore {
        override fun snapshot(): Set<SpyEntry> = set.snapshot()
        override fun contains(entry: SpyEntry) = entry in set
        override fun add(entry: SpyEntry) = set.add(entry)
        override fun remove(entry: SpyEntry) = set.remove(entry)
        override fun removeIf(predicate: (SpyEntry) -> Boolean) = set.removeIf(predicate)
    }
}
