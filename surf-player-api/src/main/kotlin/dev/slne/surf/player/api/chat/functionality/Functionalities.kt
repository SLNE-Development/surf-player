package dev.slne.surf.player.api.chat.functionality

import kotlinx.serialization.Serializable

@Serializable
data class Functionalities(
    val localChatEnabled: Boolean = true
) {
    companion object {
        val EMPTY = Functionalities()
    }
}
