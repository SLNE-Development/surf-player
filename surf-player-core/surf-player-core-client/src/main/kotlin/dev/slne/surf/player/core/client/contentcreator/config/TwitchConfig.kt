package dev.slne.surf.social.core.client.contentcreator.config

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class TwitchConfig(
    val clientId: String = "",
    val clientSecret: String = ""
)
