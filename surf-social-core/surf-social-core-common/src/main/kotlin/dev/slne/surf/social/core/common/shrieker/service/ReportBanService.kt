package dev.slne.surf.social.core.common.shrieker.service

import dev.slne.surf.api.core.serializer.java.datetime.datetime.offset.SerializableOffsetDateTime
import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.rabbitmq.api.rpc.RpcService

@RpcService
interface ReportBanService {
    suspend fun ban(
        player: SerializableUUID,
        bannedBy: SerializableUUID?,
        bannedAt: SerializableOffsetDateTime,
    )
}