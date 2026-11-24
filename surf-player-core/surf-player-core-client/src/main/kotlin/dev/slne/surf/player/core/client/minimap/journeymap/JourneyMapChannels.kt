package dev.slne.surf.player.core.client.minimap.journeymap

import dev.slne.surf.player.core.client.minimap.platform.CompanionPlayer
import dev.slne.surf.player.core.client.minimap.world.WorldInfo

/**
 * The plugin channels JourneyMap's server side uses; every packet has its own.
 */
object JourneyMapChannels {
    const val VERSION = "journeymap:version"
    const val PERMISSIONS = "journeymap:perm_req"
    const val PLAYER_LOCATIONS = "journeymap:player_loc"
    const val REMOVE_PLAYER = "journeymap:remove_player"

    /**
     * The channels the companion announces: the ones it answers on and sends on, plus the version handshake's,
     * so the client's handshake reaches a registered channel. The handshake itself is never answered.
     * The world ID channel is shared with VoxelMap.
     */
    val ANNOUNCED = listOf(VERSION, PERMISSIONS, PLAYER_LOCATIONS, REMOVE_PLAYER, WorldInfo.CHANNEL)

    fun isJourneyMap(channel: String) = channel in ANNOUNCED

    fun hasJourneyMap(player: CompanionPlayer) = player.hasChannel(PERMISSIONS)
}
