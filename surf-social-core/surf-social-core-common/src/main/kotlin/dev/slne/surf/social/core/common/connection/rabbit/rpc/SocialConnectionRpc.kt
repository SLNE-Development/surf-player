package dev.slne.surf.social.core.common.connection.rabbit.rpc

import dev.slne.surf.rabbitmq.api.rpc.RpcService
import java.util.*

@RpcService
interface SocialConnectionRpc {

    suspend fun unlinkMinecraftAccount(minecraftUuid: UUID): UnlinkResult
}
