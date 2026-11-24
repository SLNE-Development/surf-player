package dev.slne.surf.player.core.common.shrieker.service

import dev.slne.surf.rabbitmq.api.rpc.RpcService

@RpcService
interface VoiceLogService {
    suspend fun logVoiceChat(reportId: Long, voiceData: ByteArray)
}