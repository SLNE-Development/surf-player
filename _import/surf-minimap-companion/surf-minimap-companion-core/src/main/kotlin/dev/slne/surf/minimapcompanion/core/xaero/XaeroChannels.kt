package dev.slne.surf.minimapcompanion.core.xaero

/**
 * The plugin channels Xaero's World Map and Minimap listen on.
 */
object XaeroChannels {
    const val WORLD_MAP = "xaeroworldmap:main"
    const val MINIMAP = "xaerominimap:main"

    val ALL = listOf(WORLD_MAP, MINIMAP)

    fun isXaero(channel: String) = channel in ALL
}
