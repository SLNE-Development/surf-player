package dev.slne.surf.minimapcompanion.core.service

import dev.slne.surf.minimapcompanion.core.platform.CompanionPlayer
import dev.slne.surf.minimapcompanion.core.xaero.XaeroChannels
import net.kyori.adventure.key.Key
import java.util.*

/**
 * A player that records every plugin message and client effect instead of sending it.
 */
class FakePlayer(
    override val worldIdentity: String = "uid",
    override val worldKey: Key = Key.key("minecraft:overworld"),
    private val channels: Set<String> = XaeroChannels.ALL.toSet(),
    override val uuid: UUID = UUID.randomUUID(),
    override val vanillaEffectCount: Int? = 40,
) : CompanionPlayer {
    val sent = mutableListOf<Pair<String, ByteArray>>()
    val addedEffects = mutableListOf<Int>()
    val removedEffects = mutableListOf<Int>()

    /**
     * The messages sent so far, with payloads as lists so they compare by content.
     */
    val sentContent get() = sent.map { (channel, payload) -> channel to payload.toList() }

    override fun hasChannel(channel: String) = channel in channels

    override fun sendPluginMessage(channel: String, payload: ByteArray) {
        sent += channel to payload
    }

    override fun addClientEffect(networkId: Int) {
        addedEffects += networkId
    }

    override fun removeClientEffect(networkId: Int) {
        removedEffects += networkId
    }
}
