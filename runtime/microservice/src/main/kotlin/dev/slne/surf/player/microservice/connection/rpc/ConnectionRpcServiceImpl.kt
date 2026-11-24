package dev.slne.surf.player.microservice.connection.rpc

import dev.slne.surf.player.api.connection.impl.DiscordConnection
import dev.slne.surf.player.api.connection.impl.TwitchConnection
import dev.slne.surf.player.core.common.connection.rpc.ConnectionRpcService
import dev.slne.surf.player.core.common.connection.rpc.UnlinkResult
import dev.slne.surf.player.microservice.connection.repository.AccountsRepository
import java.util.*

object ConnectionRpcServiceImpl : ConnectionRpcService {
    override suspend fun findDiscordConnection(minecraftUuid: UUID): DiscordConnection? =
        AccountsRepository.findDiscordConnection(minecraftUuid)

    override suspend fun findTwitchConnection(minecraftUuid: UUID): TwitchConnection? =
        AccountsRepository.findTwitchConnection(minecraftUuid)

    override suspend fun deleteMinecraftLink(minecraftUuid: UUID): UnlinkResult =
        AccountsRepository.unlinkMinecraftAccount(minecraftUuid)
}
