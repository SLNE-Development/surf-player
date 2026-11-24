package dev.slne.surf.player.api.chat

import dev.slne.surf.player.api.chat.entry.IgnoreListEntry
import net.kyori.adventure.chat.SignedMessage
import net.kyori.adventure.text.Component
import java.util.*

/** What a single player has and can do in the chat feature. See [SurfChatApi]. */
interface ChatCapability {
    val uuid: UUID

    /** The ignore list of this player as cached on this server. */
    val cachedIgnoreList: List<IgnoreListEntry>
        get() = SurfChatApi.cachedIgnoreList(uuid)

    /** Loads the ignore list of this player and caches it on this server. */
    suspend fun ignoreList(): List<IgnoreListEntry> = SurfChatApi.ignoreList(uuid)

    /**
     * Sends [signedMessage] from this player, who must be on this server, to the player
     * identified by [targetUuid] anywhere in the network.
     */
    suspend fun sendSignedMessage(
        targetUuid: UUID,
        signedMessage: SignedMessage,
        contentComponent: Component
    ) = SurfChatApi.sendSignedMessage(uuid, targetUuid, signedMessage, contentComponent)
}
