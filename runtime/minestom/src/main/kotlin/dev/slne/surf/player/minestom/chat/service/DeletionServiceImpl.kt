package dev.slne.surf.player.minestom.chat.service

import com.google.auto.service.AutoService
import dev.slne.surf.api.core.messages.adventure.uuidOrNull
import dev.slne.surf.player.api.chat.message.MessageData
import dev.slne.surf.player.core.client.chat.message.format.createMessageDeletedTeamNotification
import dev.slne.surf.player.core.client.chat.permission.ChatPermissions
import dev.slne.surf.player.core.client.chat.platform.ChatPlatform
import dev.slne.surf.player.core.common.chat.service.DeletionService
import dev.slne.surf.player.core.common.chat.service.HistoryService
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.kyori.adventure.audience.Audience

@AutoService(DeletionService::class)
class DeletionServiceImpl : DeletionService {
    override suspend fun deleteMessage(
        message: MessageData,
        deleter: Audience?,
        deletionReason: String?,
        notifyTeam: Boolean
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
