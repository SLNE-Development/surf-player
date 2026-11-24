package dev.slne.surf.player.core.client.chat.config.configs

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class SpamConfig(
    val amount: Int = 5,
    val interval: Long = 3_000,
    val maxUppercaseCharsInRow: Int = 5,
)
