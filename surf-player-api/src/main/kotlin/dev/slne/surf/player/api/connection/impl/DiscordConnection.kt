package dev.slne.surf.player.api.connection.impl

import dev.slne.surf.player.api.connection.SocialConnection
import kotlinx.serialization.Serializable

@Serializable
data class DiscordConnection(
    val discordName: String,
    val discordId: Long
) : SocialConnection(discordName)
