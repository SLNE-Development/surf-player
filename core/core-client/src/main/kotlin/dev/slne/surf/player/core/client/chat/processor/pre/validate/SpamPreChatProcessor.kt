package dev.slne.surf.player.core.client.chat.processor.pre.validate

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Expiry
import com.sksamuel.aedile.core.expireAfterWrite
import dev.slne.surf.player.api.chat.message.MessageContext
import dev.slne.surf.player.api.chat.processor.PreChatProcessor
import dev.slne.surf.player.core.client.chat.config.chatConfig
import dev.slne.surf.player.core.client.chat.permission.ChatPermissions
import dev.slne.surf.player.core.client.chat.processor.ProcessorOrder
import dev.slne.surf.player.core.client.chat.util.hasPermission
import dev.slne.surf.player.core.client.chat.util.sendText
import java.time.Duration
import java.util.*
import kotlin.time.Duration.Companion.minutes

fun isRepeatOf(last: String?, current: String) = last != null && last.equals(current, ignoreCase = true)

/**
 * Whether the [messagesSent]-th message within one interval exceeds the limit of [amount]
 * messages per interval; exactly [amount] messages are allowed.
 */
fun exceedsRateLimit(messagesSent: Int, amount: Int) = messagesSent > amount

object SpamPreChatProcessor : PreChatProcessor {
    override val order = ProcessorOrder.VALIDATE

    private val messageRateLimit = Caffeine.newBuilder()
        .expireAfter(Expiry.creating { _, _ -> Duration.ofMillis(chatConfig.spamConfig.interval) })
        .build<UUID, Int>()

    private val lastSentMessageContent = Caffeine.newBuilder()
        .expireAfterWrite(10.minutes)
        .build<UUID, String>()

    override fun process(context: MessageContext): MessageContext {
        val messageData = context.messageData
        val sender = messageData.sender
        val message = messageData.plainMessage

        if (sender.hasPermission(ChatPermissions.BYPASS_FILTER)) {
            return context
        }

        val messagesSent = messageRateLimit.asMap().compute(sender) { _, value -> (value ?: 0) + 1 }!!

        if (exceedsRateLimit(messagesSent, chatConfig.spamConfig.amount)) {
            sender.sendText {
                appendErrorPrefix()
                error("Du sendest Nachrichten zu schnell. Bitte warte einen Moment.")
            }
            context.cancel()
            return context
        }

        var repeated = false
        lastSentMessageContent.asMap().compute(sender) { _, last ->
            if (isRepeatOf(last, message)) {
                repeated = true
                last
            } else {
                message
            }
        }

        if (repeated) {
            sender.sendText {
                appendErrorPrefix()
                error("Du darfst nicht zweimal hintereinander die gleiche Nachricht senden.")
            }
            context.cancel()
            return context
        }

        return context
    }
}
