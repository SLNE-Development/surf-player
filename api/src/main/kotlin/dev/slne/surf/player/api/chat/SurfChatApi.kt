package dev.slne.surf.player.api.chat

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.player.api.chat.entry.HistoryEntry
import dev.slne.surf.player.api.chat.entry.HistoryFilter
import dev.slne.surf.player.api.chat.entry.IgnoreListEntry
import dev.slne.surf.player.api.chat.message.MessageData
import dev.slne.surf.player.api.chat.message.redirector.MessageRedirector
import dev.slne.surf.player.api.chat.processor.PostChatProcessor
import dev.slne.surf.player.api.chat.processor.PreChatProcessor
import it.unimi.dsi.fastutil.objects.ObjectList
import kotlinx.coroutines.TimeoutCancellationException
import net.kyori.adventure.chat.SignedMessage
import net.kyori.adventure.text.Component
import java.util.*

private val instance = requiredService<SurfChatApi>()

/**
 * Entry point to chat processing, message history and ignore lists.
 *
 * Everything about a single player is also available through [ChatCapability].
 */
interface SurfChatApi {
    /** Stores [data] in the chat history and the moderation cache. */
    suspend fun logMessage(data: MessageData)

    /** Adds [processor] to the chat pipeline, ordered by [PreChatProcessor.order]. */
    fun registerChatProcessor(processor: PreChatProcessor)

    /** Adds [processor] to the end of the chat pipeline. */
    fun registerChatProcessor(processor: PostChatProcessor)

    /** Adds [redirector], which receives every chat message sent on this server. */
    fun registerMessageRedirector(redirector: MessageRedirector)

    /** The ignore list of the player identified by [uuid] as cached on this server. */
    fun cachedIgnoreList(uuid: UUID): List<IgnoreListEntry>

    /** Loads the ignore list of the player identified by [uuid] and caches it on this server. */
    suspend fun ignoreList(uuid: UUID): List<IgnoreListEntry>

    /** Deletes the signed message with [signature] for every player on this server. */
    fun deleteMessage(signature: SignedMessage.Signature)

    /** Deletes [messageData] on every server and marks it deleted by [deleter] in the history. */
    suspend fun deleteRemoteMessage(deleter: UUID?, messageData: MessageData)

    /** The chat history entries matching [filter]. */
    @Throws(TimeoutCancellationException::class)
    suspend fun lookupHistory(filter: HistoryFilter): ObjectList<HistoryEntry>

    /**
     * Sends [signedMessage] from the player identified by [senderUuid], who must be on this
     * server, to the player identified by [targetUuid] anywhere in the network.
     */
    suspend fun sendSignedMessage(
        senderUuid: UUID,
        targetUuid: UUID,
        signedMessage: SignedMessage,
        contentComponent: Component
    )

    /** Runs the AI moderation on [messageData]. */
    suspend fun passAutoMod(messageData: MessageData)

    companion object : SurfChatApi by instance {
        val INSTANCE: SurfChatApi get() = instance
    }
}
