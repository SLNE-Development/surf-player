package dev.slne.surf.player.core.client.clan.command

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HexColorTest {
    @Test
    fun `accepts a hex color of the required length in either case`() {
        assertTrue(isHexColor("FF6B6B", CLAN_TAG_COLOR_LENGTH))
        assertTrue(isHexColor("ff6b6b", CLAN_TAG_COLOR_LENGTH))
        assertTrue(isHexColor("FF6B6BCC", CLAN_TAG_SHADOW_COLOR_LENGTH))
    }

    @Test
    fun `rejects a hex color of the wrong length`() {
        assertFalse(isHexColor("FF6B6B", CLAN_TAG_SHADOW_COLOR_LENGTH))
        assertFalse(isHexColor("FF6B6BCC", CLAN_TAG_COLOR_LENGTH))
        assertFalse(isHexColor("", CLAN_TAG_COLOR_LENGTH))
    }

    @Test
    fun `rejects anything that is not made of hex digits`() {
        assertFalse(isHexColor("#FF6B6B", CLAN_TAG_COLOR_LENGTH))
        assertFalse(isHexColor("GG6B6B", CLAN_TAG_COLOR_LENGTH))
        assertFalse(isHexColor("FF 6B6B", CLAN_TAG_COLOR_LENGTH))
    }

    @Test
    fun `normalizes a hex color written with or without a number sign`() {
        assertEquals("FF6B6B", normalizeHexColor("#FF6B6B", CLAN_TAG_COLOR_LENGTH))
        assertEquals("FF6B6B", normalizeHexColor("FF6B6B", CLAN_TAG_COLOR_LENGTH))
        assertEquals("ff6b6bcc", normalizeHexColor(" #ff6b6bcc ", CLAN_TAG_SHADOW_COLOR_LENGTH))
        assertNull(normalizeHexColor("##FF6B6B", CLAN_TAG_COLOR_LENGTH))
        assertNull(normalizeHexColor("#FF6B6B", CLAN_TAG_SHADOW_COLOR_LENGTH))
    }

    @Test
    fun `every suggested color is accepted`() {
        for (color in CLAN_TAG_COLOR_SUGGESTIONS) {
            assertNotNull(normalizeHexColor(color, CLAN_TAG_COLOR_LENGTH), color)
        }
        for (color in CLAN_TAG_SHADOW_COLOR_SUGGESTIONS) {
            assertNotNull(normalizeHexColor(color, CLAN_TAG_SHADOW_COLOR_LENGTH), color)
        }
    }

    @Test
    fun `the invalid color message names the input once`() {
        val message = invalidHexColorMessage("xyz", CLAN_TAG_COLOR_LENGTH)
        assertTrue(message.startsWith("'xyz' "))
        assertFalse(message.contains("  "))
    }
}
