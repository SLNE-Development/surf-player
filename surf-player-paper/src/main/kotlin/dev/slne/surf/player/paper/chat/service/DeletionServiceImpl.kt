package dev.slne.surf.player.paper.chat.service

import com.google.auto.service.AutoService
import dev.slne.surf.api.core.messages.adventure.uuidOrNull
import dev.slne.surf.api.paper.extensions.server
import dev.slne.surf.player.api.chat.message.MessageData
import dev.slne.surf.player.core.client.chat.message.format.createMessageDeletedTeamNotification
import dev.slne.surf.player.core.common.chat.service.DeletionService
import dev.slne.surf.player.core.common.chat.service.HistoryService
import dev.slne.surf.player.paper.chat.permission.PermissionRegistry
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.kyori.adventure.audience.Audience
import org.bukkit.Bukkit

@AutoService(DeletionService::class)
class DeletionServiceImpl : DeletionService {
    override suspend fun deleteMessage(
        message: MessageData,
        deleter: Audience?,
        deletionReason: String?,
        notifyTeam: Boolean
    ): Boolean {
        val signature = message.signature ?: return false
        server.deleteMessage(signature)

        coroutineScope {
            launch {
                HistoryService.markDeleted(message.messageUuid, deleter?.uuidOrNull(), deletionReason)
            }

            if (notifyTeam) {
                val notification = createMessageDeletedTeamNotification(deleter, message)
                Bukkit.broadcast(notification, PermissionRegistry.TEAM_NOTIFY_DELETION)
            }
        }

        return true
    }
}