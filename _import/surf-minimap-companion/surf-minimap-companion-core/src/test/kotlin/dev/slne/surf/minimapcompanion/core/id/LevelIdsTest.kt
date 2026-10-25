package dev.slne.surf.minimapcompanion.core.id

import dev.slne.surf.minimapcompanion.core.config.MinimapCompanionConfig
import net.kyori.adventure.key.Key
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class LevelIdsTest {
    private val overworld = Key.key("minecraft:overworld")

    @Test
    fun `computed ids are pinned so existing maps never move`() {
        assertEquals(829346366, LevelIds.compute("", "surf:space_lobby"))
        assertEquals(-1974839840, LevelIds.compute("", "minecraft:overworld"))
        assertEquals(1011628861, LevelIds.compute("", "123e4567-e89b-12d3-a456-426614174000"))
    }

    @Test
    fun `server id changes the computed id`() {
        assertEquals(1777311968, LevelIds.compute("survival", "minecraft:overworld"))
        assertEquals(1457086701, LevelIds.compute("farmwelt", "123e4567-e89b-12d3-a456-426614174000"))
    }

    @Test
    fun `a zero digest never yields level id zero`() {
        assertEquals(1, LevelIds.fromDigest(ByteArray(32)))
    }

    @Test
    fun `an override for the world wins over the computed id`() {
        val config = MinimapCompanionConfig(worldIds = mapOf("minecraft:overworld" to 42))
        assertEquals(42, LevelIds.resolve(overworld, "any-identity", config))
    }

    @Test
    fun `an override for another world is ignored`() {
        val config = MinimapCompanionConfig(worldIds = mapOf("world" to 42))
        assertEquals(LevelIds.compute("", "uid"), LevelIds.resolve(overworld, "uid", config))
    }

    @Test
    fun `resolve mixes the configured server id into the computed id`() {
        val config = MinimapCompanionConfig(serverId = "survival")
        assertEquals(1777311968, LevelIds.resolve(overworld, "minecraft:overworld", config))
    }
}
