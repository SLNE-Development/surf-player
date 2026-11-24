package dev.slne.surf.player.core.client.chat.message

import dev.slne.surf.player.api.chat.message.MessageData
import dev.slne.surf.player.api.chat.message.MessageType
import dev.slne.surf.player.api.chat.message.MessageValidationError
import dev.slne.surf.player.api.chat.message.MessageValidationResult
import dev.slne.surf.player.core.client.chat.config.chatConfig
import dev.slne.surf.player.core.client.chat.permission.ChatPermissions
import dev.slne.surf.player.core.client.chat.platform.ChatPlatform
import dev.slne.surf.player.core.client.chat.util.hasPermission
import dev.slne.surf.player.core.common.chat.service.FunctionalityService
import java.util.UUID

fun hasTooManyCapsInARow(message: String, maxInRow: Int): Boolean {
    var consecutiveCaps = 0

    for (char in message) {
        if (char.isUpperCase()) {
            consecutiveCaps++
            if (consecutiveCaps >= maxInRow) {
                return true
            }
        } else {
            consecutiveCaps = 0
        }
    }

    return false
}

object MessageValidator {
    fun validate(messageData: MessageData): MessageValidationResult {
        val sender = messageData.sender
        val message = messageData.plainMessage

        if (sender.hasPermission(ChatPermissions.BYPASS_FILTER)) {
            return MessageValidationResult.Success()
        }

        if (this.checkAutoDisabling(sender)) {
            return MessageValidationResult.Failure(MessageValidationError.AutoDisabled())
        }

        if (messageData.type == MessageType.GLOBAL) {
            if (!FunctionalityService.getFunctionalities().localChatEnabled && !sender.hasPermission(
                    ChatPermissions.BYPASS_FUNCTIONALITY
                )
            ) {
                return MessageValidationResult.Failure(MessageValidationError.ChatDisabled())
            }
        }

        if (hasTooManyCapsInARow(message, chatConfig.spamConfig.maxUppercaseCharsInRow)) {
            return MessageValidationResult.Failure(MessageValidationError.TooManyCaps())
        }

        if (message.isBlank()) {
            return MessageValidationResult.Failure(MessageValidationError.EmptyContent())
        }

        return MessageValidationResult.Success()
    }

    fun checkAutoDisabling(player: UUID): Boolean {
        val config = chatConfig.autoDisablingConfig

        return config.enabled
                && ChatPlatform.onlinePlayerCount() > config.maximumPlayersBeforeDisable
                && !player.hasPermission(ChatPermissions.BYPASS_DISABLING)
    }
}
