package dev.slne.surf.player.core.client.connection.api

import com.google.auto.service.AutoService
import dev.slne.surf.player.api.connection.SocialConnection
import dev.slne.surf.player.api.connection.SurfConnectionApi
import dev.slne.surf.player.api.connection.impl.DiscordConnection
import dev.slne.surf.player.api.connection.impl.TwitchConnection
import dev.slne.surf.player.core.client.connection.service.SocialConnectionsService
import java.util.*

@AutoService(SurfConnectionApi::class)
class SurfConnectionApiImpl : SurfConnectionApi {
    override suspend fun <T : SocialConnection> findConnection(
        uuid: UUID,
        connectionClass: Class<T>
    ): T? = SocialConnectionsService.findConnection(connectionClass, uuid)

    override suspend fun findDiscordConnection(uuid: UUID): DiscordConnection? =
        SocialConnectionsService.findConnection(DiscordConnection::class.java, uuid)

    override suspend fun findTwitchConnection(uuid: UUID): TwitchConnection? =
        SocialConnectionsService.findConnection(TwitchConnection::class.java, uuid)
}
