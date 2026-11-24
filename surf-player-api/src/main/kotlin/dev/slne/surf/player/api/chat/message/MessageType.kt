package dev.slne.surf.player.api.chat.message

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class MessageType(val value: String) {
    companion object {
        val GLOBAL = MessageType("GLOBAL")
        val TEAM = MessageType("TEAM")
        val DIRECT = MessageType("DIRECT")
    }
}