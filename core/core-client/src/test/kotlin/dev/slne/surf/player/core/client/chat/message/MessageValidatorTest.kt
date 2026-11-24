package dev.slne.surf.player.core.client.chat.message

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MessageValidatorTest {

    @Test
    fun `exactly max capitals in a row are allowed`() {
        assertFalse(hasTooManyCapsInARow("HALLO welt", maxInRow = 5))
    }

    @Test
    fun `more than max capitals in a row are blocked`() {
        assertTrue(hasTooManyCapsInARow("HALLOO welt", maxInRow = 5))
    }

    @Test
    fun `capitals separated by other characters do not add up`() {
        assertFalse(hasTooManyCapsInARow("HALLO WELT", maxInRow = 5))
        assertFalse(hasTooManyCapsInARow("HaLLo WeLT", maxInRow = 5))
    }

    @Test
    fun `messages without capitals pass`() {
        assertFalse(hasTooManyCapsInARow("", maxInRow = 5))
        assertFalse(hasTooManyCapsInARow("hallo welt", maxInRow = 5))
    }
}
