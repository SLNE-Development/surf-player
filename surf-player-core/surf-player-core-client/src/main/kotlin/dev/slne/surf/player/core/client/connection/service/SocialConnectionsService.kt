package dev.slne.surf.player.core.client.connection.service

import dev.slne.surf.player.api.connection.SocialConnection
import dev.slne.surf.player.api.connection.impl.DiscordConnection
import dev.slne.surf.player.api.connection.impl.TwitchConnection
import dev.slne.surf.player.core.client.SurfPlayerClient
import dev.slne.surf.player.core.common.connection.rabbit.rpc.SocialConnectionRpc
import java.util.*

object SocialConnectionsService {
    private val rpc by lazy { SurfPlayerClient.rabbitApi.createRpcService<SocialConnectionRpc>() }

    @Suppress("UNCHECKED_CAST")
    suspend fun <T : SocialConnection> findConnection(
        connectionClass: Class<T>,
        minecraftUuid: UUID
    ): T? = when (connectionClass) {
        DiscordConnection::class.java -> rpc.findDiscordConnection(minecraftUuid) as? T
        TwitchConnection::class.java -> rpc.findTwitchConnection(minecraftUuid) as? T
        else -> null
    }

    suspend fun unlinkMinecraftAccount(minecraftUuid: UUID) =
        rpc.unlinkMinecraftAccount(minecraftUuid)
}
