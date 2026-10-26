package dev.slne.surf.minimapcompanion.core.xaero

import dev.slne.surf.minimapcompanion.core.config.EnforcedOptionsConfig
import dev.slne.surf.minimapcompanion.core.config.MinimapCompanionConfig
import dev.slne.surf.minimapcompanion.core.xaero.option.EnforcedOptions
import org.junit.jupiter.api.Test
import org.spongepowered.configurate.BasicConfigurationNode
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class XaeroServerProfileTest {

    private fun profile(minimap: Map<String, Any?> = emptyMap(), worldMap: Map<String, Any?> = emptyMap()) =
        XaeroServerProfile.parse(minimap, worldMap) { error("unexpected warning: $it") }

    @Test
    fun `rules allow everything by default`() {
        val profile = profile()
        assertContentEquals(XaeroPackets.minimapRules(caveMode = true, netherCaveMode = true, radar = true), profile.minimapRules())
        assertContentEquals(XaeroPackets.worldMapRules(caveMode = true, netherCaveMode = true), profile.worldMapRules())
    }

    @Test
    fun `disabling cave mode disables both cave rules`() {
        val profile = profile(
            minimap = mapOf("minimap_cave_mode_allowed" to false),
            worldMap = mapOf("cave_mode_allowed" to false),
        )
        assertContentEquals(XaeroPackets.minimapRules(caveMode = false, netherCaveMode = false, radar = true), profile.minimapRules())
        assertContentEquals(XaeroPackets.worldMapRules(caveMode = false, netherCaveMode = false), profile.worldMapRules())
    }

    @Test
    fun `allowed dimensions restrict cave mode to those dimensions`() {
        val profile = profile(
            minimap = mapOf("minimap_cave_mode_allowed_dimensions" to listOf("minecraft:the_nether")),
            worldMap = mapOf("cave_mode_allowed_dimensions" to listOf("minecraft:overworld")),
        )
        assertContentEquals(XaeroPackets.minimapRules(caveMode = false, netherCaveMode = true, radar = true), profile.minimapRules())
        assertContentEquals(XaeroPackets.worldMapRules(caveMode = true, netherCaveMode = false), profile.worldMapRules())
    }

    @Test
    fun `disabling the radar only affects the minimap rules`() {
        val profile = profile(minimap = mapOf("display_radar" to false))
        assertContentEquals(XaeroPackets.minimapRules(caveMode = true, netherCaveMode = true, radar = false), profile.minimapRules())
        assertContentEquals(XaeroPackets.worldMapRules(caveMode = true, netherCaveMode = true), profile.worldMapRules())
    }

    @Test
    fun `config channels list xaerolib, minimap and world map in order`() {
        val profile = profile(minimap = mapOf("display_radar" to false), worldMap = mapOf("waypoints" to false))
        val channels = profile.configChannels()
        assertEquals(listOf("xaerolib:main", "xaerominimap:main", "xaeroworldmap:main"), channels.map { it.first })
        assertSame(EnforcedOptions.EMPTY, channels[0].second)
        assertEquals(mapOf<String, Any>("display_radar" to false), channels[1].second.values)
        assertEquals(mapOf<String, Any>("waypoints" to false), channels[2].second.values)
    }

    @Test
    fun `profile reads the enforced options from the config`() {
        val config = MinimapCompanionConfig(
            enforcedOptions = EnforcedOptionsConfig(
                minimap = mapOf("display_radar" to BasicConfigurationNode.root().raw(false)),
                worldMap = mapOf("cave_mode_allowed_dimensions" to BasicConfigurationNode.root().raw(listOf("minecraft:overworld"))),
            )
        )
        val profile = XaeroServerProfile.of(config)
        assertEquals(mapOf<String, Any>("display_radar" to false), profile.minimap.values)
        assertEquals(setOf("minecraft:overworld"), profile.worldMap.identifiers("cave_mode_allowed_dimensions"))
    }

    @Test
    fun `profile of the same config is parsed once`() {
        val config = MinimapCompanionConfig()
        assertSame(XaeroServerProfile.of(config), XaeroServerProfile.of(config))
        assertNotSame(XaeroServerProfile.of(config), XaeroServerProfile.of(MinimapCompanionConfig()))
    }
}
