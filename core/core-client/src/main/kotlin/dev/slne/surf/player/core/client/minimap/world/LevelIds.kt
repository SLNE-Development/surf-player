package dev.slne.surf.player.core.client.minimap.world

import dev.slne.surf.player.core.client.minimap.config.MinimapCompanionConfig
import net.kyori.adventure.key.Key
import java.nio.ByteBuffer
import java.security.MessageDigest

/**
 * Derives the level ID Xaero's mods use to keep one world's map apart from another's.
 *
 * The hash input is a contract: changing it moves every player's existing maps.
 */
object LevelIds {

    /**
     * Computes the level ID of the world with [identity] on the server named [serverId].
     */
    fun compute(serverId: String, identity: String): Int {
        val input = (serverId + "\u0000" + identity).toByteArray(Charsets.UTF_8)
        return fromDigest(MessageDigest.getInstance("SHA-256").digest(input))
    }

    internal fun fromDigest(digest: ByteArray): Int {
        val id = ByteBuffer.wrap(digest, 0, 4).int
        return if (id == 0) 1 else id
    }

    /**
     * Returns the configured level ID for [worldKey], or the computed one if none is configured.
     */
    fun resolve(worldKey: Key, identity: String, config: MinimapCompanionConfig): Int =
        config.worldIds[worldKey.asString()] ?: compute(config.serverId, identity)
}
