package dev.slne.surf.social.core.client.chat.processor.pre.validate

import dev.slne.surf.social.api.chat.message.MessageContext
import dev.slne.surf.social.api.chat.processor.PreChatProcessor
import dev.slne.surf.social.core.client.chat.permission.ChatPermissions
import dev.slne.surf.social.core.client.chat.processor.ProcessorOrder
import dev.slne.surf.social.core.client.chat.util.hasPermission
import dev.slne.surf.social.core.client.chat.util.sendText
import it.unimi.dsi.fastutil.chars.CharOpenHashSet

private const val ADDITIONAL_VALID_CHARACTERS = "äöüÄÖÜß€@£¥|²³µ½¼¾«»¡¿°§´`^~¨"

private val additionalValidCharacters = CharOpenHashSet(ADDITIONAL_VALID_CHARACTERS.toCharArray())

private fun isValidCharacter(character: Char) =
    character in ' '..'~' || additionalValidCharacters.contains(character)

fun containsIllegalCharacters(message: String): Boolean {
    for (index in message.indices) {
        if (!isValidCharacter(message[index])) {
            return true
        }
    }

    return false
}

object CharPreChatProcessor : PreChatProcessor {
    override val order = ProcessorOrder.VALIDATE

    override fun process(context: MessageContext): MessageContext {
        val messageData = context.messageData

        if (context.messageData.sender.hasPermission(ChatPermissions.BYPASS_FILTER)) {
            return context
        }

        if (containsIllegalCharacters(messageData.plainMessage)) {
            messageData.sender.sendText {
                appendWarningPrefix()
                error("Deine Nachricht enthält unerlaubte Zeichen.")
            }

            context.cancel()
            return context
        }

        return context
    }
}
