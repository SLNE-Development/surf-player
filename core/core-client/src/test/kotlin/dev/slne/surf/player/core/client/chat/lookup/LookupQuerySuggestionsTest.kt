package dev.slne.surf.player.core.client.chat.lookup

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LookupQuerySuggestionsTest {
    private val players = listOf("NotAmmo", "Keviro", "FPNS")
    private val servers = listOf("lobby", "survival01")

    private fun suggest(input: String) = LookupQuerySuggestions.suggest(
        input,
        playerNames = { prefix -> players.filter { it.startsWith(prefix, ignoreCase = true) } },
        serverNames = { servers },
    )

    @Test
    fun `an empty query suggests every filter`() {
        assertEquals(LookupQuerySuggestions.filterKeys, suggest(""))
    }

    @Test
    fun `a started filter name is completed`() {
        assertEquals(listOf("--sender", "--server"), suggest("--se"))
        assertEquals(listOf("--deleted", "--deletedBy"), suggest("--del"))
    }

    @Test
    fun `filters already used are not suggested again`() {
        val suggestions = suggest("--type team ")

        assertFalse("--type team --type" in suggestions)
        assertTrue("--type team --sender" in suggestions)
    }

    @Test
    fun `the message type values are suggested`() {
        assertEquals(listOf("--type global", "--type direct", "--type team", "--type clan_chat"), suggest("--type "))
        assertEquals(listOf("--type direct"), suggest("--type d"))
    }

    @Test
    fun `ranges are suggested`() {
        assertEquals(LookupQuerySuggestions.ranges.map { "--range $it" }, suggest("--range "))
        assertEquals(listOf("--range 10m", "--range 1h", "--range 12h", "--range 1d"), suggest("--range 1"))
        assertEquals(listOf("--range 7d"), suggest("--range 7"))
    }

    @Test
    fun `player filters suggest player names`() {
        assertEquals(listOf("--sender NotAmmo"), suggest("--sender no"))
        assertEquals(players.map { "--type team --receiver $it" }, suggest("--type team --receiver "))
        assertEquals(listOf("--deletedBy Keviro"), suggest("--deletedBy K"))
    }

    @Test
    fun `server, boolean and number filters suggest their values`() {
        assertEquals(listOf("--server survival01"), suggest("--server s"))
        assertEquals(listOf("--deleted true", "--deleted false"), suggest("--deleted "))
        assertEquals(LookupQuerySuggestions.limits.map { "--limit $it" }, suggest("--limit "))
        assertEquals(LookupQuerySuggestions.pages.map { "--page $it" }, suggest("--page "))
    }

    @Test
    fun `free text values get no suggestions`() {
        assertEquals(emptyList<String>(), suggest("--message hal"))
        assertEquals(emptyList<String>(), suggest("--messageUuid 1c"))
        assertEquals(emptyList<String>(), suggest("--message \"Hallo "))
    }

    @Test
    fun `after a complete value the next filter is suggested`() {
        assertTrue("--sender NotAmmo --range" in suggest("--sender NotAmmo "))
        assertEquals(listOf("--sender NotAmmo --limit"), suggest("--sender NotAmmo --li"))
    }
}
