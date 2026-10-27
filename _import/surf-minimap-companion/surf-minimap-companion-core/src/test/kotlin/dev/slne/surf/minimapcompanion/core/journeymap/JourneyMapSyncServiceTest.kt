package dev.slne.surf.minimapcompanion.core.journeymap

import dev.slne.surf.minimapcompanion.core.config.GlobalSettingsConfig
import dev.slne.surf.minimapcompanion.core.config.JourneyMapConfig
import dev.slne.surf.minimapcompanion.core.config.MinimapCompanionConfig
import dev.slne.surf.minimapcompanion.core.service.FakePlayer
import dev.slne.surf.minimapcompanion.core.worldinfo.WorldInfo
import kotlinx.serialization.json.JsonPrimitive
import net.kyori.adventure.key.Key
import org.junit.jupiter.api.Test
import org.spongepowered.configurate.BasicConfigurationNode
import kotlin.test.assertEquals

class JourneyMapSyncServiceTest {
    private val config = MinimapCompanionConfig()
    private val journeyMap = JourneyMapConfig()

    private val lobbyWorldId = WorldInfo.CHANNEL to WorldInfo.payload("space_lobby-829346366").toList()
    private val defaultPermissions = JourneyMapChannels.PERMISSIONS to
        JourneyMapPackets.permissions(JourneyMapPermissions.json(JourneyMapProfile(emptyMap()), false, false)).toList()

    private fun lobbyPlayer() = FakePlayer(
        worldIdentity = "surf:space_lobby",
        worldKey = Key.key("surf:space_lobby"),
        channels = setOf(JourneyMapChannels.PERMISSIONS, WorldInfo.CHANNEL),
    )

    @Test
    fun `sync sends the world id, then the permissions`() {
        val player = lobbyPlayer()
        JourneyMapSyncService.sync(player, config, journeyMap, playerRadar = false)
        assertEquals(listOf(lobbyWorldId, defaultPermissions), player.sentContent)
    }

    @Test
    fun `sync applies the global settings of config yml`() {
        val player = lobbyPlayer()
        val config = MinimapCompanionConfig(globalSettings = GlobalSettingsConfig(caveMode = BasicConfigurationNode.root().raw(false)))
        JourneyMapSyncService.sync(player, config, journeyMap, playerRadar = false)

        val permissions = permissionsJson(player.sent.single { it.first == JourneyMapChannels.PERMISSIONS }.second)
        assertEquals(JsonPrimitive("NONE"), permissions["caveMapping"])
    }

    @Test
    fun `client without journeymap gets nothing`() {
        val player = FakePlayer()
        JourneyMapSyncService.sync(player, config, journeyMap, playerRadar = false)
        JourneyMapSyncService.answer(player, JourneyMapChannels.PERMISSIONS, config, journeyMap, playerRadar = false)
        assertEquals(emptyList(), player.sent)
    }

    @Test
    fun `permissions request is answered with world id and permissions`() {
        val player = lobbyPlayer()
        JourneyMapSyncService.answer(player, JourneyMapChannels.PERMISSIONS, config, journeyMap, playerRadar = false)
        assertEquals(listOf(lobbyWorldId, defaultPermissions), player.sentContent)
    }

    @Test
    fun `version handshake is never answered`() {
        val player = lobbyPlayer()
        JourneyMapSyncService.answer(player, JourneyMapChannels.VERSION, config, journeyMap, playerRadar = false)
        assertEquals(emptyList(), player.sent)
    }

    @Test
    fun `sync after disable-maps is turned off enables journeymap again`() {
        val player = lobbyPlayer()
        JourneyMapSyncService.sync(player, MinimapCompanionConfig(disableMaps = true), journeyMap, playerRadar = false)
        JourneyMapSyncService.sync(player, config, journeyMap, playerRadar = false)

        val (first, last) = player.sent.filter { it.first == JourneyMapChannels.PERMISSIONS }.map { permissionsJson(it.second) }
        assertEquals(JsonPrimitive("false"), first["journeymapEnabled"])
        assertEquals(JsonPrimitive("true"), last["journeymapEnabled"])
    }
}
