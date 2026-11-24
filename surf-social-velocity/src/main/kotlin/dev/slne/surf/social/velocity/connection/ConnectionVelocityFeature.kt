package dev.slne.surf.social.velocity.connection

import dev.slne.surf.social.velocity.VelocitySocialFeature
import dev.slne.surf.social.velocity.connection.command.linkCommand
import dev.slne.surf.social.velocity.connection.command.surfSocialCommand
import dev.slne.surf.social.velocity.connection.command.unlinkCommand
import dev.slne.surf.social.velocity.connection.config.SocialConfigManager
import dev.slne.surf.social.velocity.plugin
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*

/**
 * Social connections (Discord/Twitch account linking), formerly the standalone surf-social
 * Velocity plugin.
 */
object ConnectionVelocityFeature : VelocitySocialFeature {
    override val id = "connection"

    lateinit var socialConfigManager: SocialConfigManager
        private set

    override suspend fun onLoad() {
        socialConfigManager = SocialConfigManager(plugin.featureDataDirectory(this))
    }

    override suspend fun onEnable() {
        surfSocialCommand()
        linkCommand()
        unlinkCommand()
    }

    override suspend fun onDisable() {
        client.close()
    }
}

val client = HttpClient {
    install(ContentNegotiation) {
        json()
    }
}

val config get() = ConnectionVelocityFeature.socialConfigManager.config
