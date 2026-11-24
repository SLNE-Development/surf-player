package dev.slne.surf.social.minestom.chat.command

import dev.slne.surf.api.minestom.command.dsl.commandAPICommand
import dev.slne.surf.api.minestom.command.dsl.greedyStringArgument
import dev.slne.surf.api.minestom.command.dsl.playerExecutorSuspend
import dev.slne.surf.social.api.chat.message.MessageData
import dev.slne.surf.social.api.chat.message.MessageType
import dev.slne.surf.social.core.client.chat.permission.ChatPermissions
import dev.slne.surf.social.core.client.chat.redis.ModerationRedisService
import dev.slne.surf.social.core.client.chat.redis.event.TeamchatMessageRedisEvent
import dev.slne.surf.social.core.client.SocialClient.redisApi
import dev.slne.surf.social.core.common.chat.service.HistoryService
import dev.slne.surf.core.api.common.SurfCoreApi
import net.kyori.adventure.text.Component
import java.time.OffsetDateTime
import java.util.*

fun teamchatCommand() = commandAPICommand("teamchat") {
    withAliases("tc")
    greedyStringArgument("message")
    withPermission(ChatPermissions.COMMAND_TEAMCHAT)

    playerExecutorSuspend { player, args ->
        val message: String by args
        val messageComponent = Component.text(message)
        val messageId = UUID.randomUUID()
        val messageData = MessageData(
            messageComponent,
            messageId,
            player.uuid,
            null,
            OffsetDateTime.now(),
            SurfCoreApi.getCurrentServerName(),
            null,
            MessageType.TEAM
        )

        redisApi.publishEvent(
            TeamchatMessageRedisEvent(
                messageData
            )
        ).await()

        HistoryService.logMessage(messageData)
        ModerationRedisService.cache(messageData)
    }
}
