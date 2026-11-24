package dev.slne.surf.social.core.common.shrieker.service

import dev.slne.surf.rabbitmq.api.rpc.RpcService

@RpcService
interface VoiceLogService {
    suspend fun logVoiceChat(reportId: Long, voiceData: ByteArray)
}