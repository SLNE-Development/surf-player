package dev.slne.surf.player.api.connection

import kotlinx.serialization.Serializable

@Serializable
abstract class SocialConnection(
    val userName: String
)
