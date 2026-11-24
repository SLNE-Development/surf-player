package dev.slne.surf.player.core.client.chat.redis

import dev.slne.surf.player.core.client.chat.platform.ChatPlatform
import dev.slne.surf.player.core.client.chat.processor.post.AiModerationPostChatProcessor
import dev.slne.surf.player.core.client.chat.processor.post.PrivateMessageSpyPostChatProcessor
import dev.slne.surf.player.core.client.chat.redis.event.AiModerationNoticeRedisEvent
import dev.slne.surf.player.core.client.chat.redis.event.DeleteRemoteMessageRedisEvent
import dev.slne.surf.player.core.client.chat.redis.event.PrivateMessageSpyRedisEvent
import dev.slne.surf.player.core.client.chat.redis.event.TeamBroadcastRedisEvent
import dev.slne.surf.player.core.client.chat.service.SpyService
import dev.slne.surf.player.core.client.friends.redis.event.FriendConnectionRedisEvent
import dev.slne.surf.redis.event.OnRedisEvent

/**
 * The chat Redis events every platform handles the same way through [ChatPlatform]. The direct and
 * signed message requests need platform types and are handled by the platform modules.
 */
object ChatRedisListener {
    @OnRedisEvent
    fun onTeamBroadcast(event: TeamBroadcastRedisEvent) {
        ChatPlatform.broadcast(event.message, event.permission)
    }

    @OnRedisEvent
    fun onDeleteRemoteMessage(event: DeleteRemoteMessageRedisEvent) {
        ChatPlatform.deleteMessage(event.messageSignature)
    }

    @OnRedisEvent
    fun onAiModerationNotice(event: AiModerationNoticeRedisEvent) {
        ChatPlatform.launchAsync {
            AiModerationPostChatProcessor.showModerationNotice(event.messageData, event.action)
        }
    }

    @OnRedisEvent
    fun onPrivateMessageSpy(event: PrivateMessageSpyRedisEvent) {
        if (event.spies.none(ChatPlatform::isOnline)) return

        ChatPlatform.launchAsync {
            PrivateMessageSpyPostChatProcessor.showToLocalSpies(event.messageData, event.spies)
        }
    }

    /** A spy who leaves the network stops spying; published by the proxy on a network quit. */
    @OnRedisEvent
    fun onNetworkConnection(event: FriendConnectionRedisEvent) {
        if (!event.online) {
            SpyService.clearPrivateMessageSpies(event.playerUuid)
        }
    }
}
