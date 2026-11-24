package dev.slne.surf.player.core.client.vanish

import dev.slne.surf.player.core.client.SurfPlayerClient
import dev.slne.surf.player.core.common.playerRedisKey
import dev.slne.surf.redis.RedisApi
import dev.slne.surf.redis.sync.set.SyncSet
import java.util.*

/**
 * The network-wide set of vanished players, readable on every platform (Paper, Minestom,
 * Velocity). Only Paper changes it (its vanish feature); other features read it, e.g. so friends
 * are not told that a vanished player came online.
 *
 * Created once by [SurfPlayerClient.load], before the Redis connection is established.
 */
object VanishedPlayers {
    @Volatile
    private var players: SyncSet<UUID>? = null

    /** The synchronized set itself, for the vanish feature that changes it. */
    val syncSet: SyncSet<UUID>
        get() = players ?: error("VanishedPlayers.register has not been called yet")

    internal fun register(redisApi: RedisApi) {
        players = redisApi.createSyncSet<UUID>(playerRedisKey("vanish", "vanished-players"))
    }

    /** Whether the player identified by [uuid] is vanished anywhere in the network. */
    fun isVanished(uuid: UUID): Boolean = players?.contains(uuid) ?: false
}
