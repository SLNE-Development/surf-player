package dev.slne.surf.minimapcompanion.core.service

import dev.slne.surf.minimapcompanion.core.config.JourneyMapConfig
import dev.slne.surf.minimapcompanion.core.config.MinimapCompanionConfig
import dev.slne.surf.minimapcompanion.core.config.TrackerConfig
import dev.slne.surf.minimapcompanion.core.config.XaeroConfig
import dev.slne.surf.minimapcompanion.core.journeymap.JourneyMapChannels
import dev.slne.surf.minimapcompanion.core.journeymap.permissionsJson
import dev.slne.surf.minimapcompanion.core.xaero.XaeroChannels
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MapSyncServiceTest {
    private val xaero = XaeroConfig()
    private val journeyMap = JourneyMapConfig()

    private fun player() = FakePlayer(channels = (XaeroChannels.ALL + JourneyMapChannels.ANNOUNCED).toSet())

    private fun sync(player: FakePlayer, config: MinimapCompanionConfig = MinimapCompanionConfig(), tracksPlayers: Boolean = false) =
        MapSyncService.sync(player, config, xaero, journeyMap, tracksPlayers)

    private fun FakePlayer.playerRadar() =
        permissionsJson(sent.last { it.first == JourneyMapChannels.PERMISSIONS }.second)["worldPlayerRadar"]

    @Test
    fun `full sync reaches both mods`() {
        val player = player()
        sync(player)

        val channels = player.sent.map { it.first }.toSet()
        assertTrue(
            channels.containsAll(listOf(XaeroChannels.WORLD_MAP, XaeroChannels.LIB, JourneyMapChannels.WORLD_ID, JourneyMapChannels.PERMISSIONS)),
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
        MapSyncService.syncChannels(player, listOf(JourneyMapChannels.PERMISSIONS), MinimapCompanionConfig(), xaero, journeyMap, false)
        assertEquals(setOf(JourneyMapChannels.WORLD_ID, JourneyMapChannels.PERMISSIONS), player.sent.map { it.first }.toSet())
    }

    @Test
    fun `registering a xaero channel syncs only that channel`() {
        val player = player()
        MapSyncService.syncChannels(player, listOf(XaeroChannels.MINIMAP), MinimapCompanionConfig(), xaero, journeyMap, false)
        assertEquals(setOf(XaeroChannels.MINIMAP), player.sent.map { it.first }.toSet())
    }
}
