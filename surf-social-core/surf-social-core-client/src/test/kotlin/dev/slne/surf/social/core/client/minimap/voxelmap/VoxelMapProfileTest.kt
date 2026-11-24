package dev.slne.surf.minimapcompanion.core.voxelmap

import dev.slne.surf.minimapcompanion.core.config.VoxelMapConfig
import dev.slne.surf.minimapcompanion.core.global.GlobalFeature
import dev.slne.surf.minimapcompanion.core.global.GlobalSettings
import org.junit.jupiter.api.Test
import org.spongepowered.configurate.BasicConfigurationNode
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class VoxelMapProfileTest {
    private val warnings = mutableListOf<String>()

    private fun parse(vararg raw: Pair<String, Any?>) = VoxelMapProfile.parse(mapOf(*raw)) { warnings += it }

    @Test
    fun `booleans take yaml booleans`() {
        assertEquals(mapOf<String, Any>("cavesAllowed" to false), parse("cavesAllowed" to false).values)
        assertEquals(emptyList(), warnings)
    }

    @Test
    fun `boolean of the wrong type is skipped`() {
        assertEquals(emptyMap(), parse("cavesAllowed" to "false").values)
        assertTrue("cavesAllowed" in warnings.single(), warnings.single())
    }

    @Test
    fun `teleport command takes text without its leading slash`() {
        assertEquals(
            mapOf<String, Any>("teleportCommand" to "tp %p %x %y %z"),
            parse("teleportCommand" to "/tp %p %x %y %z").values,
        )
        assertEquals(emptyList(), warnings)

        assertEquals(emptyMap(), parse("teleportCommand" to 5).values)
        assertTrue("teleportCommand" in warnings.single(), warnings.single())
    }

    @Test
    fun `empty teleport command is skipped`() {
        assertEquals(emptyMap(), parse("teleportCommand" to " /").values)
        assertTrue("teleportCommand" in warnings.single(), warnings.single())
    }

    @Test
    fun `keys the companion sets are skipped`() {
        assertEquals(emptyMap(), parse("serverIdentity" to "x", "worldName" to "y").values)
        assertEquals(2, warnings.size)
    }

    @Test
    fun `unknown keys are skipped with a warning`() {
        assertEquals(emptyMap(), parse("fooBar" to true).values)
        assertTrue("fooBar" in warnings.single(), warnings.single())
    }

    @Test
    fun `null enforces nothing`() {
        assertEquals(emptyMap(), parse("cavesAllowed" to null).values)
        assertEquals(emptyList(), warnings)
    }

    @Test
    fun `global settings override the options before they are checked`() {
        val config = VoxelMapConfig(options = mapOf("cavesAllowed" to BasicConfigurationNode.root().raw(true)))
        val profile = VoxelMapProfile.of(config, GlobalSettings(mapOf(GlobalFeature.CAVE_MODE to false)))
        assertEquals(mapOf<String, Any>("cavesAllowed" to false), profile.values)
    }

    @Test
    fun `each config and global settings pair is parsed once`() {
        val config = VoxelMapConfig()
        val global = GlobalSettings(mapOf(GlobalFeature.RADAR to false))
        assertSame(VoxelMapProfile.of(config, global), VoxelMapProfile.of(config, global))
    }
}
