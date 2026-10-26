package dev.slne.surf.minimapcompanion.minestom.platform

import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Tracks the plugin channels each client registered. Minestom sends to any channel,
 * so the companion checks here first to reach only clients with Xaero's mods.
 */
object RegisteredChannels {
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

    fun register(playerUuid: UUID, channels: Collection<String>) {
        this.channels.computeIfAbsent(playerUuid) { ConcurrentHashMap.newKeySet() }.addAll(channels)
    }

    fun isRegistered(playerUuid: UUID, channel: String) = channels[playerUuid]?.contains(channel) == true

    fun forget(playerUuid: UUID) {
        channels.remove(playerUuid)
    }
}
