package dev.slne.surf.player.core.client.minimap.voxelmap

import dev.slne.surf.player.core.client.minimap.config.GlobalSettingsConfig
import dev.slne.surf.player.core.client.minimap.config.MinimapCompanionConfig
import dev.slne.surf.player.core.client.minimap.config.VoxelMapConfig
import dev.slne.surf.player.core.client.minimap.service.FakePlayer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import org.junit.jupiter.api.Test
import org.spongepowered.configurate.BasicConfigurationNode
import kotlin.test.assertEquals

class VoxelMapSyncServiceTest {
    private val config = MinimapCompanionConfig()
    private val voxelMap = VoxelMapConfig()

    private fun voxelMapPlayer() = FakePlayer(channels = setOf(VoxelMapChannels.SETTINGS))

    // The JSON after the packet ID and its length.
    private fun settingsJson(payload: ByteArray) =
        Json.parseToJsonElement(payload.copyOfRange(payload.indexOf('{'.code.toByte()), payload.size).decodeToString()).jsonObject

    @Test
    fun `sync sends the settings to voxelmap clients`() {
        val player = voxelMapPlayer()
        VoxelMapSyncService.sync(player, config, voxelMap)

        val expected = VoxelMapPackets.settings(VoxelMapSettings.json(VoxelMapProfile(emptyMap()), false))
        assertEquals(listOf(VoxelMapChannels.SETTINGS to expected.toList()), player.sentContent)
    }

    @Test
    fun `client without voxelmap gets nothing`() {
        val player = FakePlayer()
        VoxelMapSyncService.sync(player, config, voxelMap)
        assertEquals(emptyList(), player.sent)
    }

    private fun withGlobal(caveMode: Any, disableMaps: Boolean = false) = MinimapCompanionConfig(
        disableMaps = disableMaps,
        globalSettings = GlobalSettingsConfig(caveMode = BasicConfigurationNode.root().raw(caveMode)),
    )

    @Test
    fun `sync applies the global settings of config yml`() {
        val player = voxelMapPlayer()
        VoxelMapSyncService.sync(player, withGlobal(false), voxelMap)
        assertEquals(JsonPrimitive(false), settingsJson(player.sent.single().second)["cavesAllowed"])
    }

    @Test
    fun `disable-maps beats a global true`() {
        val player = voxelMapPlayer()
        VoxelMapSyncService.sync(player, withGlobal(true, disableMaps = true), voxelMap)
        assertEquals(JsonPrimitive(false), settingsJson(player.sent.single().second)["cavesAllowed"])
    }

    @Test
    fun `sync after disable-maps is turned off allows the maps again`() {
        val player = voxelMapPlayer()
        VoxelMapSyncService.sync(player, MinimapCompanionConfig(disableMaps = true), voxelMap)
        VoxelMapSyncService.sync(player, config, voxelMap)

        val (first, last) = player.sent.map { settingsJson(it.second) }
        assertEquals(JsonPrimitive(false), first["minimapAllowed"])
        assertEquals(JsonPrimitive(true), last["minimapAllowed"])
    }
}
