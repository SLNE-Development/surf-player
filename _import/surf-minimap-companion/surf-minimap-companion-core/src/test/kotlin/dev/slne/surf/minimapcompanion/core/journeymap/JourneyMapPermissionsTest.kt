package dev.slne.surf.minimapcompanion.core.journeymap

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class JourneyMapPermissionsTest {
    private fun json(text: String) = Json.parseToJsonElement(text)

    @Test
    fun `enabled maps without radar`() {
        assertEquals(
            json("""{"journeymapEnabled":"true","worldPlayerRadar":"NONE"}"""),
            json(JourneyMapPermissions.json(JourneyMapProfile(emptyMap()), disableMaps = false, playerRadar = false)),
        )
    }

    @Test
    fun `disable-maps turns journeymap off`() {
        assertEquals(
            json("""{"journeymapEnabled":"false","worldPlayerRadar":"NONE"}"""),
            json(JourneyMapPermissions.json(JourneyMapProfile(emptyMap()), disableMaps = true, playerRadar = false)),
        )
    }

    @Test
    fun `profile values follow the companion's fields`() {
        assertEquals(
            json("""{"journeymapEnabled":"true","worldPlayerRadar":"ALL","caveMapping":"NONE"}"""),
            json(JourneyMapPermissions.json(JourneyMapProfile(mapOf("caveMapping" to "NONE")), disableMaps = false, playerRadar = true)),
        )
    }
}
