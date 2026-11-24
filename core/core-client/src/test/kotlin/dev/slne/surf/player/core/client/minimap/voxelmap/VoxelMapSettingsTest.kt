package dev.slne.surf.player.core.client.minimap.voxelmap

import kotlinx.serialization.json.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class VoxelMapSettingsTest {
    private val allKeys = setOf("serverIdentity", "teleportCommand") + VoxelMapProfile.BOOLEANS
    private val configured = VoxelMapProfile(mapOf("cavesAllowed" to false, "teleportCommand" to "tp %p %x %y %z"))

    private fun json(profile: VoxelMapProfile, disableMaps: Boolean = false) =
        Json.parseToJsonElement(VoxelMapSettings.json(profile, disableMaps)).jsonObject

    @Test
    fun `every key is always sent`() {
        val json = json(VoxelMapProfile(emptyMap()))

        assertEquals(10, allKeys.size)
        assertEquals(allKeys, json.keys)
        assertEquals(JsonNull, json["serverIdentity"])
        assertEquals(JsonNull, json["teleportCommand"])
        for (key in VoxelMapProfile.BOOLEANS) {
            assertEquals(JsonPrimitive(true), json[key], key)
        }
    }

    @Test
    fun `every key has the type VoxelMap casts it to`() {
        for (profile in listOf(VoxelMapProfile(emptyMap()), configured)) {
            for (disableMaps in listOf(false, true)) {
                val json = json(profile, disableMaps)
                for (key in VoxelMapProfile.BOOLEANS) {
                    val value = json.getValue(key).jsonPrimitive
                    assertFalse(value.isString, key)
                    assertNotNull(value.booleanOrNull, key)
                }
            }
        }
    }

    @Test
    fun `configured values are sent`() {
        val json = json(configured)
        assertEquals(JsonPrimitive(false), json["cavesAllowed"])
        assertEquals(JsonPrimitive(true), json["minimapAllowed"])
        assertEquals(JsonPrimitive("tp %p %x %y %z"), json["teleportCommand"])
    }

    @Test
    fun `disable-maps turns every boolean off but keeps the teleport command`() {
        val json = json(configured, disableMaps = true)
        for (key in VoxelMapProfile.BOOLEANS) {
            assertEquals(JsonPrimitive(false), json[key], key)
        }
        assertEquals(JsonPrimitive("tp %p %x %y %z"), json["teleportCommand"])
    }
}
