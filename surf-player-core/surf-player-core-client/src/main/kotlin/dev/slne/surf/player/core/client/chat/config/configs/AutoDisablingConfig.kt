package dev.slne.surf.social.core.client.chat.config.configs

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class AutoDisablingConfig(
    val enabled: Boolean = false,
    val maximumPlayersBeforeDisable: Int = 50
)
