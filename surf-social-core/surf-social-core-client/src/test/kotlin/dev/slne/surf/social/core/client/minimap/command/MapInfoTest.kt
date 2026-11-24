package dev.slne.surf.social.core.client.minimap.command

import dev.slne.surf.social.core.client.minimap.config.CompanionSettings
import dev.slne.surf.social.core.client.minimap.config.GlobalSettingsConfig
import dev.slne.surf.social.core.client.minimap.config.MinimapCompanionConfig
import dev.slne.surf.social.core.client.minimap.config.XaeroConfig
import dev.slne.surf.social.core.client.minimap.global.GlobalFeature
import dev.slne.surf.social.core.client.minimap.journeymap.JourneyMapChannels
import dev.slne.surf.social.core.client.minimap.service.FakePlayer
import dev.slne.surf.social.core.client.minimap.world.LevelIds
import dev.slne.surf.social.core.client.minimap.world.WorldInfo
import dev.slne.surf.social.core.client.minimap.xaero.XaeroChannels
import dev.slne.surf.social.core.client.minimap.xaero.XaeroSyncService
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.junit.jupiter.api.Test
import org.spongepowered.configurate.BasicConfigurationNode
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MapInfoTest {
    private val settings = CompanionSettings()

    @Test
    fun `mods are read from the channels the client registered`() {
        val player = FakePlayer(channels = setOf(XaeroChannels.MINIMAP, XaeroChannels.LIB, JourneyMapChannels.PERMISSIONS))
        val info = MapInfo.of(player, "Keviro", settings)

        assertEquals(
            listOf(
                MapInfo.Mod.XAERO_MINIMAP to true,
                MapInfo.Mod.XAERO_WORLD_MAP to false,
                MapInfo.Mod.XAERO_LIB to true,
                MapInfo.Mod.JOURNEYMAP to true,
                MapInfo.Mod.VOXELMAP to false,
            ),
            info.mods,
        )
    }

    @Test
    fun `the level id and world name are the ones the mods get`() {
        val player = FakePlayer()
        val info = MapInfo.of(player, "Keviro", settings)

        val levelId = LevelIds.resolve(player.worldKey, player.worldIdentity, settings.config)
        assertEquals(levelId, info.levelId)
        assertEquals(WorldInfo.name(player.worldKey, levelId), info.worldName)
    }

    @Test
    fun `the level id last sent to xaero is shown once there is one`() {
        val player = FakePlayer()
        assertNull(MapInfo.of(player, "Keviro", settings).lastSentLevelId)

        XaeroSyncService.sync(player, XaeroChannels.ALL, settings.config, settings.xaero)
        assertEquals(XaeroSyncService.lastSentLevelId(player.uuid), MapInfo.of(player, "Keviro", settings).lastSentLevelId)
        XaeroSyncService.forget(player.uuid)
    }

    @Test
    fun `enforced options are counted per mod with the global settings applied`() {
        val settings = CompanionSettings(
            config = MinimapCompanionConfig(
                disableMaps = true,
                globalSettings = GlobalSettingsConfig(caveMode = BasicConfigurationNode.root().raw(false)),
            ),
            xaero = XaeroConfig(minimap = mapOf("display_radar" to BasicConfigurationNode.root().raw(false))),
        )
        val info = MapInfo.of(FakePlayer(), "Keviro", settings)

        assertTrue(info.disableMaps)
        assertEquals(3, info.xaeroOptions)
        assertEquals(1, info.journeyMapOptions)
        assertEquals(1, info.voxelMapOptions)
        assertEquals(mapOf(GlobalFeature.CAVE_MODE to false), info.globalOverrides)
    }

    @Test
    fun `the message names the player, the ids and the mods`() {
        val messages = mutableListOf<Component>()
        val audience = object : Audience {
            override fun sendMessage(message: Component) {
                messages += message
            }
        }
        val player = FakePlayer(channels = setOf(XaeroChannels.MINIMAP))
        val info = MapInfo.of(player, "Keviro", settings, MapInfo.Tracker(MapInfo.TrackerMode.RELATIONS, xaeroPlayers = 2))

        audience.sendMapInfo(info)
        val text = messages.joinToString("\n") { PlainTextComponentSerializer.plainText().serialize(it) }

        assertTrue("Keviro" in text, text)
        assertTrue("${info.levelId}" in text, text)
        assertTrue(info.worldName in text, text)
        assertTrue("Xaero's Minimap ✔" in text, text)
        assertTrue("JourneyMap ✘" in text, text)
        assertTrue("Freunde/Clan" in text, text)
    }
}
