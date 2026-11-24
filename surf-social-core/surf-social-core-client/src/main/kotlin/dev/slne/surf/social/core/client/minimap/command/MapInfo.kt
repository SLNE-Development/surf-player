package dev.slne.surf.social.core.client.minimap.command

import dev.slne.surf.social.core.client.minimap.config.CompanionSettings
import dev.slne.surf.social.core.client.minimap.global.GlobalFeature
import dev.slne.surf.social.core.client.minimap.journeymap.JourneyMapChannels
import dev.slne.surf.social.core.client.minimap.platform.CompanionPlayer
import dev.slne.surf.social.core.client.minimap.voxelmap.VoxelMapChannels
import dev.slne.surf.social.core.client.minimap.world.LevelIds
import dev.slne.surf.social.core.client.minimap.world.WorldInfo
import dev.slne.surf.social.core.client.minimap.xaero.XaeroChannels
import dev.slne.surf.social.core.client.minimap.xaero.XaeroSyncService

/**
 * What the companion knows and sends about one player, for `/minimapcompanion info`. Read it on the player's
 * own thread.
 */
data class MapInfo(
    val playerName: String,
    val world: String,
    val levelId: Int,
    val lastSentLevelId: Int?,
    val worldName: String,
    val mods: List<Pair<Mod, Boolean>>,
    val disableMaps: Boolean,
    val xaeroOptions: Int,
    val journeyMapOptions: Int,
    val voxelMapOptions: Int,
    val globalOverrides: Map<GlobalFeature, Boolean>,
    val tracker: Tracker?,
) {
    enum class Mod(val displayName: String) {
        XAERO_MINIMAP("Xaero's Minimap"),
        XAERO_WORLD_MAP("Xaero's World Map"),
        XAERO_LIB("Xaero's Lib"),
        JOURNEYMAP("JourneyMap"),
        VOXELMAP("VoxelMap"),
    }

    enum class TrackerMode(val displayName: String) {
        OFF("aus"),
        RELATIONS("Freunde/Clan"),
        EVERYONE("alle sehen alle"),
    }

    /**
     * The player tracker as it is for the player, on platforms that run it. [xaeroPlayers] is how many players the
     * player's Xaero map shows through it.
     */
    data class Tracker(val mode: TrackerMode, val xaeroPlayers: Int)

    companion object {
        /**
         * Collects what [player], shown as [playerName], gets under [settings].
         */
        fun of(player: CompanionPlayer, playerName: String, settings: CompanionSettings, tracker: Tracker? = null): MapInfo {
            val levelId = LevelIds.resolve(player.worldKey, player.worldIdentity, settings.config)
            return MapInfo(
                playerName = playerName,
                world = player.worldKey.asString(),
                levelId = levelId,
                lastSentLevelId = XaeroSyncService.lastSentLevelId(player.uuid),
                worldName = WorldInfo.name(player.worldKey, levelId),
                mods = listOf(
                    Mod.XAERO_MINIMAP to player.hasChannel(XaeroChannels.MINIMAP),
                    Mod.XAERO_WORLD_MAP to player.hasChannel(XaeroChannels.WORLD_MAP),
                    Mod.XAERO_LIB to player.hasChannel(XaeroChannels.LIB),
                    Mod.JOURNEYMAP to JourneyMapChannels.hasJourneyMap(player),
                    Mod.VOXELMAP to VoxelMapChannels.hasVoxelMap(player),
                ),
                disableMaps = settings.config.disableMaps,
                xaeroOptions = settings.xaeroProfile.minimap.values.size + settings.xaeroProfile.worldMap.values.size,
                journeyMapOptions = settings.journeyMapProfile.values.size,
                voxelMapOptions = settings.voxelMapProfile.values.size,
                globalOverrides = settings.global.overrides,
                tracker = tracker,
            )
        }
    }
}
