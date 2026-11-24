package dev.slne.surf.player.core.client.chat.message

import dev.slne.surf.player.api.chat.message.redirector.MessageRedirector
import java.util.concurrent.CopyOnWriteArrayList

/**
 * The redirectors every chat message sent on this server is passed to.
 */
object MessageRedirectorRegistry {
    val redirectors = CopyOnWriteArrayList<MessageRedirector>()

    fun clear() {
        redirectors.clear()
    }

    fun register(redirector: MessageRedirector) {
        redirectors.add(redirector)
    }
}
