package dev.slne.surf.player.api.chat.message.redirector

import dev.slne.surf.player.api.chat.message.MessageData
import net.kyori.adventure.chat.SignedMessage

interface MessageRedirector {
    suspend fun redirectMessage(signedMessage: SignedMessage, messageData: MessageData)
}