package dev.slne.surf.social.core.client.minimap.xaero

/**
 * The plugin channels Xaero's World Map, Minimap and Xaero's Lib listen on.
 */
object XaeroChannels {
    const val WORLD_MAP = "xaeroworldmap:main"
    const val MINIMAP = "xaerominimap:main"
    const val LIB = "xaerolib:main"

    /**
     * The channels of the two map mods, which share their packets.
     */
    val MAPS = listOf(WORLD_MAP, MINIMAP)

    val ALL = MAPS + LIB

    fun isXaero(channel: String) = channel in ALL

    fun isMap(channel: String) = channel in MAPS
}
