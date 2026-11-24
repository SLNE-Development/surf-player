package dev.slne.surf.player.tests.bot

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Raw NBT payloads of `custom` click events, referenced from the adventure click event by a token
 * (see the replaced `ClickEventSerializerImpl` in src/main/java).
 */
object CustomClickPayloads {
    private const val PREFIX = "playertests-payload#"
    private val payloads = ConcurrentHashMap<String, Any>()
    private val ids = AtomicLong()

    @JvmStatic
    fun store(payload: Any): String {
        val token = PREFIX + ids.incrementAndGet()
        payloads[token] = payload
        return token
    }

    @JvmStatic
    fun get(token: String): Any? = payloads[token]
}
