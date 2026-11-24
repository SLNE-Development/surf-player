package dev.slne.surf.social.microservice.connection.rpc

import dev.slne.surf.social.core.common.connection.rabbit.rpc.SocialConnectionRpc
import dev.slne.surf.social.core.common.connection.rabbit.rpc.UnlinkResult
import dev.slne.surf.social.microservice.connection.repository.AccountsRepository
import java.util.*

object SocialConnectionRpcImpl : SocialConnectionRpc {
    override suspend fun unlinkMinecraftAccount(minecraftUuid: UUID): UnlinkResult {
        return AccountsRepository.unlinkMinecraftAccount(minecraftUuid)
    }
}
