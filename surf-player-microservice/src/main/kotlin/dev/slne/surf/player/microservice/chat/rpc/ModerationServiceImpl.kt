package dev.slne.surf.player.microservice.chat.rpc

import dev.slne.surf.player.api.chat.message.MessageData
import dev.slne.surf.player.core.common.chat.aimoderation.ModerationClassificationResult
import dev.slne.surf.player.core.common.chat.rabbit.rpc.ModerationService
import dev.slne.surf.player.microservice.chat.table.ModerationsTable
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.insert
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction

object ModerationServiceImpl : ModerationService {
    override suspend fun logModeration(
        messageData: MessageData,
        classification: ModerationClassificationResult
    ) = suspendTransaction {
        ModerationsTable.insert {
            it[messageUuid] = messageData.messageUuid
            it[action] = classification.action
            it[flaggedScores] = classification.flaggedScores
        }
        Unit
    }
}