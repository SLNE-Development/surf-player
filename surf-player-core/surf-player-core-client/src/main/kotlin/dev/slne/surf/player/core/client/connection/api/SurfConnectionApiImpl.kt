package dev.slne.surf.social.core.client.connection.api

import com.google.auto.service.AutoService
import dev.slne.surf.social.api.connection.SurfConnectionApi
import dev.slne.surf.social.api.connection.SocialConnection
import dev.slne.surf.social.core.client.connection.service.SocialConnectionsService
import java.util.*

@AutoService(SurfConnectionApi::class)
class SurfConnectionApiImpl : SurfConnectionApi {
    override suspend fun <T : SocialConnection> findConnection(
        connectionClass: Class<T>,
        minecraftUuid: UUID
    ): T? = SocialConnectionsService.findConnection(connectionClass, minecraftUuid)
}
