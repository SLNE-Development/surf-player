package dev.slne.surf.social.microservice.connection.rpc

import dev.slne.surf.social.core.common.connection.rabbit.rpc.SocialConnectionRpc
import dev.slne.surf.social.microservice.connection.repository.AccountsRepository
import java.util.*

class SocialConnectionRpcImpl : SocialConnectionRpc {
    override suspend fun unlinkMinecraftAccount(minecraftUuid: UUID): SocialConnectionRpc.UnlinkResult {
        return AccountsRepository.unlinkMinecraftAccountWithResult(minecraftUuid)
    }
}