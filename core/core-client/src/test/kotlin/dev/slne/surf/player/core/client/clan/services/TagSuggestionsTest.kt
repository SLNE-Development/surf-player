package dev.slne.surf.player.core.client.clan.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TagSuggestionsTest {
    @Test
    fun `an empty input has its own bucket`() {
        assertEquals("", tagSuggestionBucketKey(""))
    }

    @Test
    fun `the bucket is the first three characters of the input`() {
        assertEquals("T", tagSuggestionBucketKey("T"))
        assertEquals("TCL", tagSuggestionBucketKey("TCL"))
        assertEquals("TCL", tagSuggestionBucketKey("TCLX"))
    }

    @Test
    fun `an empty input suggests every tag of its bucket`() {
        assertEquals(listOf("ABC", "TCL"), filterTagSuggestions(listOf("ABC", "TCL"), "", 100))
    }

    @Test
    fun `only tags starting with the input are suggested, at most the limit`() {
        val bucket = listOf("TC2", "TC5", "TCL", "TCLX")

        assertEquals(listOf("TCL", "TCLX"), filterTagSuggestions(bucket, "TCL", 100))
        assertEquals(listOf("TC2"), filterTagSuggestions(bucket, "TC", 1))
    }
}
