package dev.slne.surf.player.core.client.clan.tagcolor

import dev.slne.surf.player.api.clan.clan.Clan
import dev.slne.surf.player.api.clan.clan.ClanTagColor
import dev.slne.surf.player.api.clan.clan.update
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.ShadowColor
import net.kyori.adventure.text.format.TextColor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * `/clan color` failed with a `ClassCastException` (`TextColorImpl` to `Void`) whenever a field of the
 * update was left unset or reset; these run every combination through [ClanTagColor.applyUpdate].
 */
class ClanTagColorUpdateTest {
    private val current = ClanTagColor(
        foregroundColor = NamedTextColor.RED,
        backgroundColor = NamedTextColor.BLUE,
        shadowColor = ShadowColor.shadowColor(0x11223344),
    )

    @Test
    fun `setting the foreground keeps the other colors`() {
        val updated = current.applyUpdate(ClanTagColor.update { foreground(TextColor.color(0xFF6B6B)) })

        assertEquals(TextColor.color(0xFF6B6B), updated.foregroundColor)
        assertEquals(current.backgroundColor, updated.backgroundColor)
        assertEquals(current.shadowColor, updated.shadowColor)
    }

    @Test
    fun `setting the background keeps the other colors`() {
        val updated = current.applyUpdate(ClanTagColor.update { background(TextColor.color(0x1A1A2E)) })

        assertEquals(current.foregroundColor, updated.foregroundColor)
        assertEquals(TextColor.color(0x1A1A2E), updated.backgroundColor)
        assertEquals(current.shadowColor, updated.shadowColor)
    }

    @Test
    fun `setting the shadow keeps the other colors`() {
        val shadow = ShadowColor.shadowColor(0xFF6B6BCC.toInt())
        val updated = current.applyUpdate(ClanTagColor.update { shadow(shadow) })

        assertEquals(current.foregroundColor, updated.foregroundColor)
        assertEquals(current.backgroundColor, updated.backgroundColor)
        assertEquals(shadow, updated.shadowColor)
    }

    @Test
    fun `resetting goes back to the default colors`() {
        val updated = current.applyUpdate(ClanTagColor.update {
            resetForeground()
            resetBackground()
            resetShadow()
        })

        assertEquals(Clan.DEFAULT_CLAN_TAG_COLORS, updated)
    }

    @Test
    fun `a clan without colors starts from the defaults`() {
        val none: ClanTagColor? = null
        val updated = none.update(ClanTagColor.update { foreground(NamedTextColor.GOLD) })

        assertEquals(NamedTextColor.GOLD, updated.foregroundColor)
        assertEquals(Clan.DEFAULT_CLAN_TAG_BACKGROUND_COLOR, updated.backgroundColor)
        assertEquals(Clan.DEFAULT_CLAN_TAG_SHADOW_COLOR, updated.shadowColor)
    }
}
