package dev.slne.surf.minimapcompanion.core.service

import dev.slne.surf.minimapcompanion.core.config.JourneyMapConfig
import dev.slne.surf.minimapcompanion.core.config.MinimapCompanionConfig
import dev.slne.surf.minimapcompanion.core.config.TrackerConfig
import dev.slne.surf.minimapcompanion.core.config.VoxelMapConfig
import dev.slne.surf.minimapcompanion.core.config.XaeroConfig
import dev.slne.surf.minimapcompanion.core.journeymap.JourneyMapChannels
import dev.slne.surf.minimapcompanion.core.journeymap.permissionsJson
import dev.slne.surf.minimapcompanion.core.voxelmap.VoxelMapChannels
import dev.slne.surf.minimapcompanion.core.worldinfo.WorldInfo
import dev.slne.surf.minimapcompanion.core.xaero.XaeroChannels
import kotlinx.serialization.json.JsonPrimitive
import net.kyori.adventure.key.Key
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MapSyncServiceTest {
    private val xaero = XaeroConfig()
    private val journeyMap = JourneyMapConfig()
    private val voxelMap = VoxelMapConfig()

    private fun player() =
        FakePlayer(channels = (XaeroChannels.ALL + JourneyMapChannels.ANNOUNCED + VoxelMapChannels.SETTINGS).toSet())

    private fun sync(player: FakePlayer, config: MinimapCompanionConfig = MinimapCompanionConfig(), tracksPlayers: Boolean = false) =
        MapSyncService.sync(player, config, xaero, journeyMap, voxelMap, tracksPlayers)

    private fun FakePlayer.playerRadar() =
        permissionsJson(sent.last { it.first == JourneyMapChannels.PERMISSIONS }.second)["worldPlayerRadar"]

    @Test
    fun `full sync reaches every mod`() {
        val player = player()
        sync(player)

        val channels = player.sent.map { it.first }.toSet()
        assertTrue(
            channels.containsAll(
                listOf(XaeroChannels.WORLD_MAP, XaeroChannels.LIB, WorldInfo.CHANNEL, JourneyMapChannels.PERMISSIONS, VoxelMapChannels.SETTINGS)
            ),
            channels.toString(),
        )
    }

    @Test
    fun `player radar is off where the platform does not track players`() {
        val withFriends = MinimapCompanionConfig(tracker = TrackerConfig(friends = true))

        val minestom = player()
        sync(minestom, withFriends, tracksPlayers = false)
        assertEquals(JsonPrimitive("NONE"), minestom.playerRadar())

        val paper = player()
        sync(paper, withFriends, tracksPlayers = true)
        assertEquals(JsonPrimitive("ALL"), paper.playerRadar())

        val paperWithoutTracker = player()
        sync(paperWithoutTracker, MinimapCompanionConfig(), tracksPlayers = true)
        assertEquals(JsonPrimitive("NONE"), paperWithoutTracker.playerRadar())
    }

    @Test
    fun `registering journeymap's permission channel syncs only journeymap`() {
        val player = player()
        MapSyncService.syncChannels(player, listOf(JourneyMapChannels.PERMISSIONS), MinimapCompanionConfig(), xaero, journeyMap, voxelMap, false)
        assertEquals(setOf(WorldInfo.CHANNEL, JourneyMapChannels.PERMISSIONS), player.sent.map { it.first }.toSet())
    }

    @Test
    fun `world id requests are answered for every client that asks`() {
        val player = FakePlayer(
            worldIdentity = "surf:space_lobby",
            worldKey = Key.key("surf:space_lobby"),
            channels = setOf(WorldInfo.CHANNEL),
        )
        MapSyncService.answer(player, WorldInfo.CHANNEL, MinimapCompanionConfig(), journeyMap, false)
        assertEquals(listOf(WorldInfo.CHANNEL to WorldInfo.payload("space_lobby-829346366").toList()), player.sentContent)
    }

    @Test
    fun `full sync sends voxelmap its settings but no world id`() {
        val player = FakePlayer(channels = setOf(VoxelMapChannels.SETTINGS, WorldInfo.CHANNEL))
        sync(player)
        // The platforms drop messages on channels the client did not register, such as Xaero's here.
        assertEquals(setOf(VoxelMapChannels.SETTINGS), player.sent.map { it.first }.filter(player::hasChannel).toSet())
    }

    @Test
    fun `registering voxelmap's settings channel syncs only voxelmap`() {
        val player = player()
        MapSyncService.syncChannels(player, listOf(VoxelMapChannels.SETTINGS), MinimapCompanionConfig(), xaero, journeyMap, voxelMap, false)
        assertEquals(setOf(VoxelMapChannels.SETTINGS), player.sent.map { it.first }.toSet())
    }

    @Test
    fun `registering a xaero channel syncs only that channel`() {
        val player = player()
        MapSyncService.syncChannels(player, listOf(XaeroChannels.MINIMAP), MinimapCompanionConfig(), xaero, journeyMap, voxelMap, false)
        assertEquals(setOf(XaeroChannels.MINIMAP), player.sent.map { it.first }.toSet())
    }
}
