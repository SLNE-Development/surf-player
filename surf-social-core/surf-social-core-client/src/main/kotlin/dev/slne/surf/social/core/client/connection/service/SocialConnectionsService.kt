package dev.slne.surf.social.core.client.connection.service

import dev.slne.surf.social.api.connection.SocialConnection
import dev.slne.surf.social.api.connection.impl.DiscordConnection
import dev.slne.surf.social.api.connection.impl.TwitchConnection
import dev.slne.surf.social.core.client.SocialClient
import dev.slne.surf.social.core.common.connection.rabbit.rpc.SocialConnectionRpc
import java.util.*

object SocialConnectionsService {
    private val rpc by lazy { SocialClient.rabbitApi.createRpcService<SocialConnectionRpc>() }

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
