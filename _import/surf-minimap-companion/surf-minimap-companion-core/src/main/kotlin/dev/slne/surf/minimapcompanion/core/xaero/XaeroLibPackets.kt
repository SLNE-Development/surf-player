package dev.slne.surf.minimapcompanion.core.xaero

import dev.slne.surf.minimapcompanion.core.xaero.nbt.*

/**
 * Builds the payloads Xaero's Lib reads on [XaeroChannels.LIB]. Xaero's Lib carries the config
 * a server enforces for each of Xaero's mods.
 */
object XaeroLibPackets {
    private const val SERVER_HANDSHAKE: Byte = 0
    private const val DIMENSION_HANDSHAKE: Byte = 1
    private const val CONFIG_CHANNEL = 2
    private const val ENFORCED_CONFIG = 3
    private const val PROFILE_INFO = 5

    /**
     * Resets the config the client received from the server before.
     */
    fun serverHandshake() = byteArrayOf(SERVER_HANDSHAKE, 1)

    /**
     * Tells the client the server has Xaero's Lib in its current world. Without it the client
     * drops the enforced config once the world border arrives.
     */
    fun dimensionHandshake() = byteArrayOf(DIMENSION_HANDSHAKE, 1)

    /**
     * Selects the config channel, such as `xaerominimap:main`, the following config packets apply to.
     */
    fun configChannel(channelId: String) = packet(CONFIG_CHANNEL, nbtCompound("c" to NbtString(channelId)))

    /**
     * Enforces [options], keyed by Xaero's option IDs. With [reset] the client first drops what was enforced before.
     */
    fun enforcedConfig(options: NbtCompound, reset: Boolean) = packet(
        ENFORCED_CONFIG, nbtCompound(
            "o" to options,
            "r" to NbtByte.of(reset),
        )
    )

    /**
     * Tells the client there are no server config profiles it may edit.
     */
    fun profileInfoReset() = packet(
        PROFILE_INFO, nbtCompound(
            "e" to NbtList(emptyList()),
            "r" to NbtByte.of(true),
        )
    )

    private fun packet(index: Int, tag: NbtTag) = byteArrayOf(index.toByte()) + tag.toNetworkBytes()
}
