package dev.slne.surf.social.microservice.connection.rpc

import dev.slne.surf.social.api.connection.impl.DiscordConnection
import dev.slne.surf.social.api.connection.impl.TwitchConnection
import dev.slne.surf.social.core.common.connection.rabbit.rpc.SocialConnectionRpc
import dev.slne.surf.social.core.common.connection.rabbit.rpc.UnlinkResult
import dev.slne.surf.social.microservice.connection.repository.AccountsRepository
import java.util.*

object SocialConnectionRpcImpl : SocialConnectionRpc {
    override suspend fun findDiscordConnection(minecraftUuid: UUID): DiscordConnection? =
        AccountsRepository.findDiscordConnection(minecraftUuid)

    override suspend fun findTwitchConnection(minecraftUuid: UUID): TwitchConnection? =
        AccountsRepository.findTwitchConnection(minecraftUuid)

    override suspend fun unlinkMinecraftAccount(minecraftUuid: UUID): UnlinkResult =
        AccountsRepository.unlinkMinecraftAccount(minecraftUuid)
}
