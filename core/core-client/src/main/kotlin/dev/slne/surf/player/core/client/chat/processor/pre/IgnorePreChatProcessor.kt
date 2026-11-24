package dev.slne.surf.player.core.client.chat.processor.pre

import dev.slne.surf.api.core.util.logger
import dev.slne.surf.player.api.chat.message.MessageContext
import dev.slne.surf.player.api.chat.processor.PreChatProcessor
import dev.slne.surf.player.core.client.chat.permission.ChatPermissions
import dev.slne.surf.player.core.client.chat.platform.ChatPlatform
import dev.slne.surf.player.core.client.chat.processor.ProcessorOrder
import dev.slne.surf.player.core.client.chat.rpc.chatIgnoreRpcService
import dev.slne.surf.player.core.client.chat.service.IgnoreRules
import dev.slne.surf.player.core.client.chat.service.IgnoreService
import kotlinx.coroutines.withTimeoutOrNull
import java.util.*
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds

/**
 * Cancels direct messages to a receiver who ignores the sender, on this or another server; the
 * sender then gets the neutral undeliverable message.
 *
 * The check runs on the sender's server before the signed message is shown to the sender and
 * forwarded, so a blocked message is never echoed or delivered.
 */
object IgnorePreChatProcessor : PreChatProcessor {
    private val log = logger()
    private val lookupTimeout = 5.seconds

    override val order = ProcessorOrder.IGNORE

    override fun process(context: MessageContext): MessageContext {
        val data = context.messageData
        val receiver = data.receiver

        if (receiver != null && IgnoreService.blocksMessage(receiver, data.sender)) {
            context.cancel()
        }

        return context
    }

    override suspend fun processAsync(context: MessageContext): MessageContext {
        val data = context.messageData
        val receiver = data.receiver ?: return context
        val sender = data.sender

        val blocked = IgnoreRules.blocksDirectMessage(
            receiverOnThisServer = ChatPlatform.isOnline(receiver),
            senderExempt = { ChatPlatform.hasPermission(sender, ChatPermissions.IGNORE_EXEMPT) },
            cachedIgnore = { IgnoreService.isIgnored(receiver, sender) },
            storedIgnore = { storedIgnore(receiver, sender) },
        )

        if (blocked) {
            context.cancel()
        }

        return context
    }

    /** Whether [receiver] ignores [sender] as stored by the microservice; `false` if it does not answer in time. */
    private suspend fun storedIgnore(receiver: UUID, sender: UUID): Boolean {
        val ignoring = try {
            withTimeoutOrNull(lookupTimeout) { chatIgnoreRpcService.isIgnoring(receiver, sender) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            log.atWarning().withCause(e).log("Failed to look up whether %s ignores %s", receiver, sender)
            null
        }

        if (ignoring == null) {
            log.atWarning().log("Could not check whether %s ignores %s; delivering the direct message", receiver, sender)
        }

        return ignoring ?: false
    }
}
