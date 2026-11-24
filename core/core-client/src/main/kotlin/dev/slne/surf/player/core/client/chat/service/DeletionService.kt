package dev.slne.surf.player.core.client.chat.service

import dev.slne.surf.api.core.messages.adventure.uuidOrNull
import dev.slne.surf.player.api.chat.message.MessageData
import dev.slne.surf.player.core.client.SurfPlayerClient
import dev.slne.surf.player.core.client.chat.message.format.createMessageDeletedTeamNotification
import dev.slne.surf.player.core.client.chat.permission.ChatPermissions
import dev.slne.surf.player.core.client.chat.redis.event.DeleteRemoteMessageRedisEvent
import dev.slne.surf.player.core.client.chat.redis.event.TeamBroadcastRedisEvent
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.kyori.adventure.audience.Audience

/**
 * Deletes chat messages on every server and marks them deleted in the history.
 */
object DeletionService {
    /**
     * Deletes [message] for every player on every server (a signed message may have been shown on
     * other servers too, e.g. as clan chat or direct message), marks it deleted in the history and,
     * if [notifyTeam], tells the team on every server.
     *
     * @return `false` if [message] has no signature and cannot be deleted
     */
    suspend fun deleteMessage(
        message: MessageData,
        deleter: Audience? = null,
        deletionReason: String? = null,
        notifyTeam: Boolean = true
    ): Boolean {
        val signature = message.signature ?: return false
        val redisApi = SurfPlayerClient.redisApi

        coroutineScope {
            // Handled by every server including this one, see ChatRedisListener.
            launch { redisApi.publishEvent(DeleteRemoteMessageRedisEvent(signature)).await() }

            launch {
                HistoryService.markDeleted(
                    message.messageUuid,
                    deleter?.uuidOrNull(),
                    deletionReason
                )
            }

            if (notifyTeam) {
                launch {
                    val notification = createMessageDeletedTeamNotification(deleter, message)
                    redisApi.publishEvent(
                        TeamBroadcastRedisEvent(notification, ChatPermissions.TEAM_NOTIFY_DELETION)
                    ).await()
                }
            }
        }

        return true
    }
}
