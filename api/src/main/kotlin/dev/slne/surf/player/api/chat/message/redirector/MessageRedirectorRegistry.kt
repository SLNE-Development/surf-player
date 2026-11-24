package dev.slne.surf.player.api.chat.message.redirector

import java.util.concurrent.CopyOnWriteArrayList

object MessageRedirectorRegistry {
    val redirectors = CopyOnWriteArrayList<MessageRedirector>()

    fun clear() {
        redirectors.clear()
    }

    fun register(redirector: MessageRedirector) {
        redirectors.add(redirector)
    }
}