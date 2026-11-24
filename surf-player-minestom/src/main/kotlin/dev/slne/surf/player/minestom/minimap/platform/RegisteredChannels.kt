package dev.slne.surf.player.minestom.minimap.platform

import dev.slne.surf.player.core.client.minimap.journeymap.JourneyMapChannels
import dev.slne.surf.player.core.client.minimap.voxelmap.VoxelMapChannels
import dev.slne.surf.player.core.client.minimap.xaero.XaeroChannels
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Tracks which of the map mods' channels each client registered. Minestom sends to any channel,
 * so the companion checks here first to reach only clients with the mod.
 */
object RegisteredChannels {
    /**
     * The channels of the supported map mods, which the companion announces and asks clients about.
     */
    val MOD_CHANNELS = XaeroChannels.ALL + JourneyMapChannels.ANNOUNCED + VoxelMapChannels.SETTINGS

    private val modChannels = MOD_CHANNELS.toSet()
    private val channels = ConcurrentHashMap<UUID, MutableSet<String>>()

    /**
     * Reads the channel names from a `minecraft:register` payload, which separates them with null bytes.
     */
    fun parseRegisterPayload(payload: ByteArray): List<String> =
        payload.toString(Charsets.UTF_8).split('\u0000').filter { it.isNotEmpty() }

    /**
     * Builds the `minecraft:register` payload that announces [channels] to a client.
     */
    fun registerPayload(channels: Collection<String>): ByteArray =
        channels.joinToString("\u0000").toByteArray(Charsets.UTF_8)

    /**
     * Remembers the [channels] of supported mods among the ones a client registered. Anything else is dropped, so a
     * client cannot grow this with names it makes up.
     */
    fun register(playerUuid: UUID, channels: Collection<String>) {
        val known = channels.filter { it in modChannels }
        if (known.isEmpty()) return
        this.channels.computeIfAbsent(playerUuid) { ConcurrentHashMap.newKeySet() }.addAll(known)
    }

    fun isRegistered(playerUuid: UUID, channel: String) = channels[playerUuid]?.contains(channel) == true

    fun forget(playerUuid: UUID) {
        channels.remove(playerUuid)
    }
}
