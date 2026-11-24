package dev.slne.surf.social.core.client.util

import dev.slne.surf.api.core.util.logger
import java.util.concurrent.CopyOnWriteArrayList

@PublishedApi
internal val listenerLog = logger()

/**
 * Listeners of type [L] that are notified in isolation: a listener that throws is logged and does
 * not keep the remaining listeners from being notified.
 */
class ListenerRegistry<L : Any> {
    @PublishedApi
    internal val listeners = CopyOnWriteArrayList<L>()

    fun register(listener: L) {
        listeners.add(listener)
    }

    fun unregister(listener: L) {
        listeners.remove(listener)
    }

    /**
     * Calls [call] on every registered listener that is a [T].
     */
    inline fun <reified T : L> notify(call: (T) -> Unit) {
        for (listener in listeners) {
            if (listener !is T) continue

            try {
                call(listener)
            } catch (e: Throwable) {
                listenerLog.atWarning()
                    .withCause(e)
                    .log("Failed to invoke listener %s", listener.javaClass.name)
            }
        }
    }
}
