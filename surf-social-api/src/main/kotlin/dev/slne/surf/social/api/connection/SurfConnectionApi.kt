package dev.slne.surf.social.api.connection

import dev.slne.surf.api.core.util.requiredService
import java.util.*

private val api = requiredService<SurfConnectionApi>()

interface SurfConnectionApi {
    suspend fun <T : SocialConnection> findConnection(
        connectionClass: Class<T>,
        minecraftUuid: UUID
    ): T?

    companion object : SurfConnectionApi by api
}

suspend inline fun <reified T : SocialConnection> SurfConnectionApi.findConnection(
    minecraftUuid: UUID
): T? = findConnection(T::class.java, minecraftUuid)