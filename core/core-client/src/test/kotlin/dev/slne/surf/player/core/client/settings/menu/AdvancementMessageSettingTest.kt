package dev.slne.surf.player.core.client.settings.menu

import dev.slne.surf.player.api.settings.setting.AdvancementMessageVisibility
import dev.slne.surf.player.api.settings.setting.SettingKeys
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AdvancementMessageSettingTest {
    private val key = SettingKeys.CHAT_ADVANCEMENT_MESSAGES
    private val display = settingKeyMappings.getValue(key.name)

    @Test
    fun `all shows every advancement`() {
        assertTrue(AdvancementMessageVisibility.ALL.shows(ownAdvancement = true))
        assertTrue(AdvancementMessageVisibility.ALL.shows(ownAdvancement = false))
    }

    @Test
    fun `own shows only own advancements`() {
        assertTrue(AdvancementMessageVisibility.OWN.shows(ownAdvancement = true))
        assertFalse(AdvancementMessageVisibility.OWN.shows(ownAdvancement = false))
    }

    @Test
    fun `none shows no advancement`() {
        assertFalse(AdvancementMessageVisibility.NONE.shows(ownAdvancement = true))
        assertFalse(AdvancementMessageVisibility.NONE.shows(ownAdvancement = false))
    }

    @Test
    fun `enum key round trips and falls back to default for unknown values`() {
        assertEquals("OWN", key.serialize(AdvancementMessageVisibility.OWN))
        assertEquals(AdvancementMessageVisibility.OWN, key.deserialize("own"))
        assertEquals(AdvancementMessageVisibility.ALL, key.deserialize("true"))
    }

    @Test
    fun `menu offers every visibility and cycles through them`() {
        assertEquals(
            AdvancementMessageVisibility.entries.map { it.name },
            display.options.map { it.value },
        )
        assertEquals("OWN", display.nextValue("ALL"))
        assertEquals("NONE", display.nextValue("OWN"))
        assertEquals("ALL", display.nextValue("NONE"))
        assertEquals("Nur eigene", display.optionOf("own")?.label)
        assertNull(display.optionOf("true"))
    }

    @Test
    fun `boolean settings keep their two options`() {
        val deathMessages = settingKeyMappings.getValue(SettingKeys.CHAT_DEATH_MESSAGES.name)
        assertEquals("false", deathMessages.nextValue("true"))
        assertEquals("true", deathMessages.nextValue("false"))
        assertEquals("Aktiviert", deathMessages.optionOf("TRUE")?.label)
    }
}
