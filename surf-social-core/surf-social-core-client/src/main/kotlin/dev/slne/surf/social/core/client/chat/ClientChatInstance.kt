package dev.slne.surf.social.core.client.chat

import dev.slne.surf.social.core.client.SocialClient
import dev.slne.surf.social.core.common.chat.ChatInstance
import dev.slne.surf.social.core.common.chat.rabbit.rpc.ModerationService
import dev.slne.surf.rabbitmq.api.ClientRabbitMQApi
import dev.slne.surf.redis.RedisApi

interface ClientChatInstance : ChatInstance {
    override val rabbitApi: ClientRabbitMQApi get() = SocialClient.rabbitApi
    override val redisApi: RedisApi get() = SocialClient.redisApi

    val moderationService: ModerationService

    companion object : ClientChatInstance by ChatInstance.INSTANCE as ClientChatInstance {
        val INSTANCE get() = ChatInstance.INSTANCE
    }
}
