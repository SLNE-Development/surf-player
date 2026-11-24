package dev.slne.surf.player.velocity.connection

import dev.slne.surf.player.velocity.VelocityPlayerFeature
import dev.slne.surf.player.velocity.connection.command.connectionAdminActions
import dev.slne.surf.player.velocity.connection.command.linkCommand
import dev.slne.surf.player.velocity.connection.command.unlinkCommand
import dev.slne.surf.player.velocity.connection.config.SocialConfigManager
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*

/**
 * Social connections (Discord/Twitch account linking), formerly the standalone surf-player
 * Velocity plugin.
 */
object ConnectionVelocityFeature : VelocityPlayerFeature {
    override val id = "connection"

    lateinit var socialConfigManager: SocialConfigManager
        private set

    override suspend fun onLoad() {
        socialConfigManager = SocialConfigManager(dataDirectory)
    }

    override suspend fun onEnable() {
        linkCommand()
        unlinkCommand()
    }

    override fun adminActions() = connectionAdminActions()

    override suspend fun onDisable() {
        httpClient.close()
    }
}

/**
 * The HTTP client the connection feature talks to the CastCrafter auth service with.
 */
val httpClient = HttpClient {
    install(ContentNegotiation) {
        json()
    }
}

val config get() = ConnectionVelocityFeature.socialConfigManager.config
