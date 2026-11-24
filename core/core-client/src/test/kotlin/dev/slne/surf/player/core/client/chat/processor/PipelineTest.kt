package dev.slne.surf.player.core.client.chat.processor

import dev.slne.surf.player.api.chat.message.MessageContext
import dev.slne.surf.player.api.chat.message.MessageData
import dev.slne.surf.player.api.chat.message.MessageType
import dev.slne.surf.player.api.chat.processor.PostChatProcessor
import dev.slne.surf.player.core.client.chat.config.isAiModerationActive
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.util.*

class PipelineTest {
    private val context = MessageContext(
        MessageData(
            Component.text("hallo"),
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            OffsetDateTime.now(),
            "lobby",
            null,
            MessageType.DIRECT
        ),
        false,
        mutableSetOf()
    )

    @Test
    fun `a failing post processor neither fails the caller nor stops the others`() {
        val ran = mutableListOf<String>()
        val processors = listOf(
            PostChatProcessor { ran += "first" },
            PostChatProcessor { error("This request requires apiKey or workloadIdentity") },
            PostChatProcessor { ran += "last" },
        )

        runBlocking { runPostProcessors(context, processors) }

        assertEquals(listOf("first", "last"), ran)
    }

    @Test
    fun `cancellation is not swallowed`() {
        val processors = listOf(PostChatProcessor { throw CancellationException("cancelled") })

        assertThrows(CancellationException::class.java) {
            runBlocking { runPostProcessors(context, processors) }
        }
    }

    @Test
    fun `the AI moderation is only active with an API key`() {
        assertFalse(isAiModerationActive(enabled = true, apiKey = ""))
        assertFalse(isAiModerationActive(enabled = true, apiKey = "  "))
        assertFalse(isAiModerationActive(enabled = false, apiKey = "sk-test"))
        assertTrue(isAiModerationActive(enabled = true, apiKey = "sk-test"))
    }
}
