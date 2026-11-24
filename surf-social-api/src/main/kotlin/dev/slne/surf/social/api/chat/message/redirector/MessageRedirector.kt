package dev.slne.surf.social.api.chat.message.redirector

import dev.slne.surf.social.api.chat.message.MessageData
import net.kyori.adventure.chat.SignedMessage

interface MessageRedirector {
    suspend fun redirectMessage(signedMessage: SignedMessage, messageData: MessageData)
}