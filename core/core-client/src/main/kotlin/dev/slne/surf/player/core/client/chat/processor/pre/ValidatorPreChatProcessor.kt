package dev.slne.surf.player.core.client.chat.processor.pre

import dev.slne.surf.player.core.client.chat.processor.pre.validate.ChatRejectionMessages
import dev.slne.surf.player.api.chat.message.MessageContext
import dev.slne.surf.player.api.chat.processor.PreChatProcessor
import dev.slne.surf.player.core.client.chat.message.MessageValidator
import dev.slne.surf.player.core.client.chat.processor.ProcessorOrder
import dev.slne.surf.player.core.client.chat.util.sendText

object ValidatorPreChatProcessor : PreChatProcessor {
    override val order = ProcessorOrder.VALIDATE

    override fun process(context: MessageContext): MessageContext {
        val data = context.messageData
        val senderUuid = data.sender
        val validationResult = MessageValidator.validate(data)

        if (validationResult.isFailure()) {
            val error = validationResult.getErrorOrThrow()

            senderUuid.sendText {
                append(ChatRejectionMessages.validationFailed(error))
            }

            context.cancel()
        }

        return context
    }
}
