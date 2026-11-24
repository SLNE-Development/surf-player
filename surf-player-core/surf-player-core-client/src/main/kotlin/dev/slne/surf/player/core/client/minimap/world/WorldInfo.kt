package dev.slne.surf.player.core.client.minimap.world

import dev.slne.surf.player.core.client.minimap.config.MinimapCompanionConfig
import dev.slne.surf.player.core.client.minimap.platform.CompanionPlayer
import net.kyori.adventure.key.Key

/**
 * The world ID JourneyMap and VoxelMap ask for on `worldinfo:world_id`. Both keep a world's map data and
 * waypoints apart by it, so every mod gets the same name for a world.
 *
 * The name is a contract: changing it moves every player's existing maps.
 */
object WorldInfo {
    const val CHANNEL = "worldinfo:world_id"

    // JourneyMap starts its payloads with this marker byte; VoxelMap expects it too.
    private const val MARKER = 42
    private const val MAX_WORLD_LENGTH = 48
    private val NOT_ALLOWED = Regex("[^a-z0-9_]")

    /**
     * Names the world [worldKey] with [levelId], the ID Xaero gets for it, as `<world>-<id>`,
     * for example `overworld-2320127456`.
     */
    fun name(worldKey: Key, levelId: Int): String {
        val world = worldKey.value().lowercase().replace(NOT_ALLOWED, "_").take(MAX_WORLD_LENGTH)
        return "$world-${levelId.toUInt()}"
    }

    /**
     * The answer carrying [name]. VoxelMap reads its length as one byte and JourneyMap as a VarInt,
     * which is the same byte while the name stays under 128 bytes.
     */
    fun payload(name: String): ByteArray {
        val utf8 = name.toByteArray(Charsets.UTF_8)
        require(utf8.size < 128) { "World ID name is ${utf8.size} bytes, but must stay under 128: $name" }
        return byteArrayOf(0, MARKER.toByte(), utf8.size.toByte()) + utf8
    }

    /**
     * Sends [player] the name of their current world. The level ID is resolved like Xaero's,
     * so server-id and world-ids apply here too.
     */
    fun send(player: CompanionPlayer, config: MinimapCompanionConfig) {
        val levelId = LevelIds.resolve(player.worldKey, player.worldIdentity, config)
        player.sendPluginMessage(CHANNEL, payload(name(player.worldKey, levelId)))
    }
}
