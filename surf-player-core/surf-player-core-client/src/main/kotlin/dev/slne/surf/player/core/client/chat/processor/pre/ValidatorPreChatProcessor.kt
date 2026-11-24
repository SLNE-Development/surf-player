package dev.slne.surf.social.core.client.chat.processor.pre

import dev.slne.surf.api.core.messages.Colors
import dev.slne.surf.social.api.chat.message.MessageContext
import dev.slne.surf.social.api.chat.processor.PreChatProcessor
import dev.slne.surf.social.core.client.chat.message.MessageValidator
import dev.slne.surf.social.core.client.chat.processor.ProcessorOrder
import dev.slne.surf.social.core.client.chat.util.sendText

object ValidatorPreChatProcessor : PreChatProcessor {
    override val order = ProcessorOrder.VALIDATE

    override fun process(context: MessageContext): MessageContext {
        val data = context.messageData
        val senderUuid = data.sender
        val validationResult = MessageValidator.validate(data)

        if (validationResult.isFailure()) {
            val error = validationResult.getErrorOrThrow()

            senderUuid.sendText {
                appendWarningPrefix()
                append(error.errorMessage.colorIfAbsent(Colors.ERROR))
            }

            context.cancel()
        }

        return context
    }
}
