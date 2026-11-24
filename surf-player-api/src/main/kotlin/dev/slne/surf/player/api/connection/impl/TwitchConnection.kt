package dev.slne.surf.player.api.connection.impl

import dev.slne.surf.player.api.connection.SocialConnection
import kotlinx.serialization.Serializable

@Serializable
data class TwitchConnection(
    val twitchName: String,
    val twitchId: Long
) : SocialConnection(twitchName)