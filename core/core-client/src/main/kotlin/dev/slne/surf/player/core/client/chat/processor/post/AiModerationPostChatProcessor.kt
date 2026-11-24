package dev.slne.surf.player.core.client.chat.processor.post

import dev.slne.surf.api.core.messages.Colors
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.core.util.logger
import dev.slne.surf.player.api.chat.message.MessageContext
import dev.slne.surf.player.api.chat.message.MessageData
import dev.slne.surf.player.api.chat.processor.PostChatProcessor
import dev.slne.surf.player.core.client.SurfPlayerClient.redisApi
import dev.slne.surf.player.core.client.chat.ai.OpenAiService
import dev.slne.surf.player.core.client.chat.config.aiModerationConfig
import dev.slne.surf.player.core.client.chat.message.format.appendBotIcon
import dev.slne.surf.player.core.client.chat.permission.ChatPermissions
import dev.slne.surf.player.core.client.chat.platform.ChatPlatform
import dev.slne.surf.player.core.client.chat.redis.event.AiModerationNoticeRedisEvent
import dev.slne.surf.player.core.client.chat.rpc.chatModerationRpcService
import dev.slne.surf.player.core.client.chat.util.hasPermission
import dev.slne.surf.player.core.client.chat.webhook.DiscordClient
import dev.slne.surf.player.core.client.chat.webhook.DiscordMessages
import dev.slne.surf.player.core.common.chat.aimoderation.ModerationClassificationAction
import dev.slne.surf.player.core.common.chat.aimoderation.ModerationClassificationResult
import dev.slne.surf.player.core.client.chat.service.DeletionService
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.punish.api.common.punishment.PunishType
import dev.slne.surf.punish.api.common.user.PunishmentUser
import net.kyori.adventure.text.event.ClickEvent
import java.net.URI
import java.time.OffsetDateTime
import java.util.UUID

object AiModerationPostChatProcessor : PostChatProcessor {
    private val log = logger()

    override suspend fun process(messageContext: MessageContext) {
        if (messageContext.isCancelled) {
            return
        }

        if (!aiModerationConfig.enabled) {
            return
        }

        processMessage(messageContext.messageData)
    }

    private suspend fun postWebhook(
        messageData: MessageData,
        classification: ModerationClassificationResult
    ) = runCatching {
        val senderName = nameOrUuid(messageData.sender)
        val receiverName = messageData.receiver?.let { nameOrUuid(it) }

        val jsonPayload = DiscordMessages.moderationModerated(
            messageData,
            classification,
            senderName,
            receiverName
        )

        DiscordClient(URI.create(aiModerationConfig.webhookUrl).toURL()).use { client ->
            if (!client.sendJson(jsonPayload)) {
                log.atWarning().log("Discord API rejected the AI moderation webhook request.")
            }
        }
    }.onFailure {
        log.atWarning().withCause(it).log("Failed to send webhook for AI moderation")
    }

    private suspend fun nameOrUuid(uuid: UUID): String {
        return SurfCoreApi.getOfflinePlayer(uuid)?.lastKnownName ?: uuid.toString()
    }

    suspend fun processMessage(messageData: MessageData) {
        if (messageData.sender.hasPermission(ChatPermissions.BYPASS_FILTER)) {
            return
        }

        val plain = messageData.plainMessage
        val classification = OpenAiService.classifyChatMessage(plain, messageData.type.value)

        if (classification.action == ModerationClassificationAction.NONE) {
            return
        }

        postWebhook(messageData, classification)

        // Every server renders the notice itself, see AiModerationNoticeRedisEvent.
        redisApi.publishEvent(AiModerationNoticeRedisEvent(messageData, classification.action)).await()

        when (classification.action) {
            ModerationClassificationAction.DELETE -> {
                deleteMessage(messageData, classification.action)
            }

            ModerationClassificationAction.MUTE -> {
                deleteMessage(messageData, classification.action)

                if (aiModerationConfig.autoMuteEnabled) {
                    mutePlayer(messageData, classification)
                }
            }

            else -> Unit
        }

        runCatching {
            chatModerationRpcService.createModerationLog(messageData, classification)
        }.onFailure { log.atWarning().withCause(it).log("Failed to log AI moderation") }
    }

    /**
     * Shows the moderation notice for [messageData], flagged with [action], to the team members on
     * this server. The delete button of a [ModerationClassificationAction.SILENT_FLAG] deletes the
     * message on every server.
     */
    suspend fun showModerationNotice(messageData: MessageData, action: ModerationClassificationAction) {
        val plain = messageData.plainMessage
        val name = messageData.senderUser().lastKnownName ?: messageData.sender.toString()

        val notice = buildText {
            appendBotIcon()
            info("Die Nachricht von ")
            variableValue(name)
            info(" wurde von der KI-Moderation markiert: ")
            spacer("(${messageData.type.value}) ")

            append {
                text(plain.take(20), Colors.WHITE)
                if (plain.length > 20) {
                    text("...", Colors.GRAY)
                }

                hoverEvent(buildText {
                    text(plain, Colors.WHITE)
                })
            }

            if (action == ModerationClassificationAction.SILENT_FLAG) {
                append {
                    spacer(" [")
                    text("LÖSCHEN", Colors.RED)
                    spacer("]")
                    hoverEvent(
                        buildText {
                            text("Klicke hier, um die Nachricht zu löschen", Colors.RED)
                        }
                    )
                    clickEvent(ClickEvent.callback { clicked ->
                        ChatPlatform.launchAsync {
                            val deleted = DeletionService.deleteMessage(
                                messageData,
                                deleter = clicked,
                                deletionReason = "AI classification: ${action.name}",
                                notifyTeam = false
                            )

                            if (!deleted) {
                                clicked.sendText {
                                    appendErrorPrefix()
                                    error("Die Nachricht konnte nicht gelöscht werden!")
                                }
                            }
                        }
                    })
                }
            }
        }

        ChatPlatform.broadcast(notice, ChatPermissions.TEAM_NOTIFY_MODERATION)
    }

    private suspend fun deleteMessage(
        messageData: MessageData,
        action: ModerationClassificationAction
    ) {
        DeletionService.deleteMessage(
            messageData,
            deletionReason = "AI classification: ${action.name}",
        )
    }

    private suspend fun mutePlayer(
        messageData: MessageData,
        classification: ModerationClassificationResult
    ) {
        PunishmentUser.byUuid(messageData.sender)
            .punish(PunishType.MUTE.Expirable(OffsetDateTime.now().plusDays(aiModerationConfig.autoMuteDurationDays)) {
                note(buildString {
                    append("[AI MODERATION] Schwerwiegendes Chatverhalten: [")
                    val scores = classification.flaggedScores.entries
                        .sortedByDescending { it.value }

                    for (entry in scores) {
                        val scorePercent = entry.value * 100
                        append("${entry.key.name}=${"%.2f".format(scorePercent)} %")
                        if (entry != scores.last()) {
                            append(", ")
                        }
                    }

                    append("]")
                })
            }, "Schwerwiegendes unangemessenes Chatverhalten")
    }
}
