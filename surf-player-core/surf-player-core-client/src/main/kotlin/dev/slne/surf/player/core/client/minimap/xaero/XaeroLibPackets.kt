package dev.slne.surf.player.core.client.minimap.xaero

import dev.slne.surf.player.core.client.minimap.xaero.nbt.toNetworkBytes
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.nbt.ListBinaryTag

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
    fun configChannel(channelId: String) = packet(CONFIG_CHANNEL, CompoundBinaryTag.builder().putString("c", channelId).build())

    /**
     * Enforces [options], keyed by Xaero's option IDs. With [reset] the client first drops what was enforced before.
     */
    fun enforcedConfig(options: CompoundBinaryTag, reset: Boolean) = packet(
        ENFORCED_CONFIG, CompoundBinaryTag.builder()
            .put("o", options)
            .putBoolean("r", reset)
            .build()
    )

    /**
     * Tells the client there are no server config profiles it may edit.
     */
    fun profileInfoReset() = packet(
        PROFILE_INFO, CompoundBinaryTag.builder()
            .put("e", ListBinaryTag.empty())
            .putBoolean("r", true)
            .build()
    )

    private fun packet(index: Int, tag: CompoundBinaryTag) = byteArrayOf(index.toByte()) + tag.toNetworkBytes()
}
