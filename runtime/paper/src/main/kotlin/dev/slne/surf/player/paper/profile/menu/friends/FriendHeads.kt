package dev.slne.surf.player.paper.profile.menu.friends

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.papermc.paper.datacomponent.item.ResolvableProfile
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withTimeoutOrNull
import org.bukkit.Bukkit
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

/**
 * The player heads of the friends menus. Offline players have no skin on the server, so their
 * profile is resolved against the Mojang session server before a menu opens ([preload]) and kept
 * for a while; [head] then answers from that cache.
 */
@Suppress("UnstableApiUsage")
object FriendHeads {
    private val resolved: Cache<UUID, ResolvableProfile> = Caffeine.newBuilder()
        .maximumSize(2_000)
        .expireAfterWrite(30, TimeUnit.MINUTES)
        .build()

    /** Resolves the skins of [players] (UUID to name) that are not cached yet, in parallel. */
    suspend fun preload(players: Map<UUID, String>) {
        coroutineScope {
            players.filterKeys { resolved.getIfPresent(it) == null }
                .map { (uuid, name) -> async { resolve(uuid, name) } }
                .awaitAll()
        }
    }

    /** The profile for the head of [uuid]: the resolved one if known, an unresolved one otherwise. */
    fun head(uuid: UUID, name: String): ResolvableProfile =
        resolved.getIfPresent(uuid) ?: unresolved(uuid, name)

    private suspend fun resolve(uuid: UUID, name: String) {
        // Online players carry their textures already.
        Bukkit.getPlayer(uuid)?.let {
            resolved.put(uuid, ResolvableProfile.resolvableProfile(it.playerProfile))
            return
        }

        val profile = withTimeoutOrNull(5.seconds) { unresolved(uuid, name).resolve().await() } ?: return
        resolved.put(uuid, ResolvableProfile.resolvableProfile(profile))
    }

    private fun unresolved(uuid: UUID, name: String): ResolvableProfile =
        ResolvableProfile.resolvableProfile(Bukkit.createProfile(uuid, name))
}
