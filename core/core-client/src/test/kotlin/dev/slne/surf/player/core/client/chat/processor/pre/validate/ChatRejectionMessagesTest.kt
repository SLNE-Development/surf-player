package dev.slne.surf.player.core.client.chat.processor.pre.validate

import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.player.api.chat.message.MessageValidationError
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ChatRejectionMessagesTest {
    private val json = GsonComponentSerializer.gson()
    private val errorPrefix = json.serialize(buildText { appendErrorPrefix() }.children().first())
    private val warningPrefix = json.serialize(buildText { appendWarningPrefix() }.children().first())

    private fun allRejections(): List<Component> = listOf(
        ChatRejectionMessages.DISALLOWED_LINK,
        ChatRejectionMessages.ILLEGAL_CHARACTERS,
        ChatRejectionMessages.validationFailed(MessageValidationError.EmptyContent()),
        ChatRejectionMessages.validationFailed(MessageValidationError.TooManyCaps()),
        ChatRejectionMessages.validationFailed(MessageValidationError.AutoDisabled()),
        ChatRejectionMessages.validationFailed(MessageValidationError.ChatDisabled()),
    )

    @Test
    fun `the prefixes differ`() {
        assertFalse(errorPrefix == warningPrefix)
    }

    @Test
    fun `every rejection of a chat message starts with the error prefix`() {
        for (message in allRejections()) {
            val rendered = json.serialize(message)
            assertTrue(rendered.contains(errorPrefix), "no error prefix in $rendered")
            assertFalse(rendered.contains(warningPrefix), "warning prefix in $rendered")
        }
    }
}
