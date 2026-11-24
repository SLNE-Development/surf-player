package dev.slne.surf.player.core.client.chat.service

import dev.slne.surf.api.core.messages.adventure.uuidOrNull
import dev.slne.surf.player.api.chat.message.MessageData
import dev.slne.surf.player.core.client.chat.message.format.createMessageDeletedTeamNotification
import dev.slne.surf.player.core.client.chat.permission.ChatPermissions
import dev.slne.surf.player.core.client.chat.platform.ChatPlatform
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.kyori.adventure.audience.Audience

/**
 * Deletes chat messages on this server and marks them deleted in the history.
 */
object DeletionService {
    suspend fun deleteMessage(
        message: MessageData,
        deleter: Audience? = null,
        deletionReason: String? = null,
        notifyTeam: Boolean = true
    ): Boolean {
        val signature = message.signature ?: return false
        ChatPlatform.deleteMessage(signature)

        coroutineScope {
            launch {
                HistoryService.markDeleted(
                    message.messageUuid,
                    deleter?.uuidOrNull(),
                    deletionReason
                )
            }

            if (notifyTeam) {
                val notification =
                    createMessageDeletedTeamNotification(deleter, message)
                ChatPlatform.broadcast(notification, ChatPermissions.TEAM_NOTIFY_DELETION)
            }
        }

        return true
    }
}
