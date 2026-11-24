package dev.slne.surf.player.core.client.chat.processor.pre.validate

import com.github.benmanes.caffeine.cache.Caffeine
import dev.slne.surf.player.api.chat.message.MessageContext
import dev.slne.surf.player.api.chat.processor.PreChatProcessor
import dev.slne.surf.player.core.client.chat.config.chatConfig
import dev.slne.surf.player.core.client.chat.permission.ChatPermissions
import dev.slne.surf.player.core.client.chat.processor.ProcessorOrder
import dev.slne.surf.player.core.client.chat.util.hasPermission
import dev.slne.surf.player.core.client.chat.util.sendText
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

fun isRepeatOf(last: String?, current: String) = last != null && last.equals(current, ignoreCase = true)

/**
 * Whether the [messagesSent]-th message within one interval exceeds the limit of [amount]
 * messages per interval; exactly [amount] messages are allowed.
 */
fun exceedsRateLimit(messagesSent: Int, amount: Int) = messagesSent > amount

/** What the spam protection decided for one message. */
enum class SpamVerdict {
    ALLOWED,

    /** The same message as the last one the sender sent; takes precedence over [TOO_FAST]. */
    REPEATED,

    /** More messages within one interval than allowed. */
    TOO_FAST,
}

/**
 * The spam protection's memory of what every sender sent.
 *
 * Only allowed messages count: a blocked message neither counts towards the rate limit nor
 * becomes the "last message" a repeat is compared to. A repeat is checked first, so spamming the
 * same message only ever produces the repeat notice, one per attempt.
 *
 * @param intervalMillis the length of one rate limit interval; it starts with the first counted
 * message
 * @param amount how many messages one interval allows
 * @param repeatMemory how long the last message is remembered for the repeat check
 * @param now the clock, in milliseconds
 */
class SpamGuard(
    private val intervalMillis: () -> Long,
    private val amount: () -> Int,
    private val repeatMemory: Duration = 10.minutes,
    private val now: () -> Long = System::currentTimeMillis,
) {
    private class State(var windowStart: Long) {
        var count = 0
        var lastMessage: String? = null
        var lastSentAt = 0L
    }

    private val states = Caffeine.newBuilder()
        .expireAfterAccess(repeatMemory.toJavaDuration())
        .build<UUID, State>()

    /** Decides whether [sender] may send [message] and, if so, remembers it. */
    fun check(sender: UUID, message: String): SpamVerdict {
        var verdict = SpamVerdict.ALLOWED

        states.asMap().compute(sender) { _, existing ->
            val time = now()
            val state = existing ?: State(time)

            if (state.lastMessage != null && time - state.lastSentAt >= repeatMemory.inWholeMilliseconds) {
                state.lastMessage = null
            }

            if (time - state.windowStart >= intervalMillis()) {
                state.windowStart = time
                state.count = 0
            }

            verdict = when {
                isRepeatOf(state.lastMessage, message) -> SpamVerdict.REPEATED
                exceedsRateLimit(state.count + 1, amount()) -> SpamVerdict.TOO_FAST
                else -> {
                    if (state.count == 0) state.windowStart = time
                    state.count++
                    state.lastMessage = message
                    state.lastSentAt = time
                    SpamVerdict.ALLOWED
                }
            }

            state
        }

        return verdict
    }
}

object SpamPreChatProcessor : PreChatProcessor {
    override val order = ProcessorOrder.VALIDATE

    private val guard = SpamGuard(
        intervalMillis = { chatConfig.spamConfig.interval },
        amount = { chatConfig.spamConfig.amount },
    )

    override fun process(context: MessageContext): MessageContext {
        val messageData = context.messageData
        val sender = messageData.sender

        if (sender.hasPermission(ChatPermissions.BYPASS_FILTER)) {
            return context
        }

        val notice = when (guard.check(sender, messageData.plainMessage)) {
            SpamVerdict.ALLOWED -> return context
            SpamVerdict.REPEATED -> "Du darfst nicht zweimal hintereinander die gleiche Nachricht senden."
            SpamVerdict.TOO_FAST -> "Du sendest Nachrichten zu schnell. Bitte warte einen Moment."
        }

        sender.sendText {
            appendErrorPrefix()
            error(notice)
        }
        context.cancel()

        return context
    }
}
