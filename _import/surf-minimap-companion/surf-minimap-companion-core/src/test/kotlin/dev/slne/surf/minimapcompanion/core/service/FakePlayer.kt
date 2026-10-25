package dev.slne.surf.minimapcompanion.core.service

import dev.slne.surf.minimapcompanion.core.platform.CompanionPlayer
import net.kyori.adventure.key.Key
import java.util.*

/**
 * A player that records every plugin message instead of sending it.
 */
class FakePlayer(
    override val worldIdentity: String = "uid",
    override val worldKey: Key = Key.key("minecraft:overworld"),
) : CompanionPlayer {
    override val uuid: UUID = UUID.randomUUID()

    val sent = mutableListOf<Pair<String, ByteArray>>()

    override fun sendPluginMessage(channel: String, payload: ByteArray) {
        sent += channel to payload
    }
}
