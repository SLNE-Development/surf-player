package dev.slne.surf.player.microservice.connection.config

import dev.slne.surf.api.core.util.logger
import java.nio.file.Path
import java.util.*
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.writeText

object SocialConfig {
    private const val TOKEN_PLACEHOLDER = "YOUR_TOKEN_HERE"
    private val log = logger()

    @Volatile
    lateinit var discordBotToken: String
        private set

    fun load(dataPath: Path) {
        val configFile = dataPath.resolve("social.properties")

        if (!configFile.exists()) {
            dataPath.createDirectories()
            configFile.writeText(
                """
                |# Discord Bot Token for fetching user information
                |discord-bot-token=$TOKEN_PLACEHOLDER
                """.trimMargin()
            )
            log.atWarning().log("Config file created at %s. Please configure your Discord bot token.", configFile)
        }

        val properties = Properties()
        configFile.inputStream().use { properties.load(it) }

        discordBotToken = properties.getProperty("discord-bot-token", "")

        if (discordBotToken.isBlank() || discordBotToken == TOKEN_PLACEHOLDER) {
            log.atSevere().log("Discord bot token is not configured! Please set it in %s", configFile)
        }
    }
}
