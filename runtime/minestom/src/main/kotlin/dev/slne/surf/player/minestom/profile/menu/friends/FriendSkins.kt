package dev.slne.surf.player.minestom.profile.menu.friends

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import dev.slne.surf.api.minestom.extension.ConnectionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import net.minestom.server.entity.PlayerSkin
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

/**
 * The skins on the player heads of the friends menus. Offline players have no skin on the
 * server, so it is fetched from Mojang before a menu opens ([preload]) and kept for a while;
 * [skin] then answers from that cache.
 */
object FriendSkins {
    private val fetched: Cache<UUID, PlayerSkin> = Caffeine.newBuilder()
        .maximumSize(2_000)
        .expireAfterWrite(30, TimeUnit.MINUTES)
        .build()

    /** Fetches the skins of [players] that are offline and not cached yet, in parallel. */
    suspend fun preload(players: Collection<UUID>) {
        coroutineScope {
            players.filter { ConnectionManager.getOnlinePlayerByUuid(it) == null && fetched.getIfPresent(it) == null }
                .map { uuid -> async { fetch(uuid) } }
                .awaitAll()
        }
    }

    /** The skin of [uuid]: the one of the connected player, else the fetched one, `null` if unknown. */
    fun skin(uuid: UUID): PlayerSkin? =
        ConnectionManager.getOnlinePlayerByUuid(uuid)?.skin ?: fetched.getIfPresent(uuid)

    private suspend fun fetch(uuid: UUID) {
        val skin = withTimeoutOrNull(5.seconds) {
            withContext(Dispatchers.IO) { PlayerSkin.fromUuid(uuid.toString()) }
        } ?: return

        fetched.put(uuid, skin)
    }
}
