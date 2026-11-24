package dev.slne.surf.player.core.client.vanish

import dev.slne.surf.player.core.client.SurfPlayerClient
import dev.slne.surf.player.core.common.playerRedisKey
import dev.slne.surf.redis.RedisApi
import dev.slne.surf.redis.sync.map.SyncMap
import java.util.*

/**
 * The network-wide vanished players with the priority each one is hidden with (see
 * [VanishSight.targetPriority]), readable on every platform (Paper, Minestom, Velocity). Only
 * Paper changes it (its vanish feature); other features read it, e.g. so friends are only told
 * that a vanished player came online if they may see them.
 *
 * Created once by [SurfPlayerClient.load], before the Redis connection is established.
 */
object VanishedPlayers {
    @Volatile
    private var players: SyncMap<UUID, Int>? = null

    /** The synchronized map itself (uuid to priority), for the vanish feature that changes it. */
    val syncMap: SyncMap<UUID, Int>
        get() = players ?: error("VanishedPlayers.register has not been called yet")

    internal fun register(redisApi: RedisApi) {
        players = redisApi.createSyncMap<UUID, Int>(playerRedisKey("vanish", "vanished-players"))
    }

    /** Whether the player identified by [uuid] is vanished anywhere in the network. */
    fun isVanished(uuid: UUID): Boolean = players?.containsKey(uuid) ?: false

    /** The priority the player identified by [uuid] is vanished with, `null` if they are not vanished. */
    fun vanishPriority(uuid: UUID): Int? = players?.get(uuid)

    /**
     * [vanishPriority], asking Redis when the local view of this server does not list [uuid]: the
     * local view may not have received a vanish that just happened on another server yet. For
     * decisions that must never reveal a vanished player, e.g. the friend count of the join greeting.
     */
    suspend fun committedVanishPriority(uuid: UUID): Int? {
        val map = players ?: return null
        return map[uuid] ?: map.getRemote(uuid)
    }
}
