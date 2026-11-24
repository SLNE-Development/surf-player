package dev.slne.surf.player.velocity.queue.config

import dev.slne.surf.api.core.config.SpongeYmlConfigClass
import dev.slne.surf.player.velocity.queue.QueueVelocityFeature
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class VelocityQueueConfig(
    val reconnect: ReconnectConfig = ReconnectConfig(),
) {
    companion object : SpongeYmlConfigClass<VelocityQueueConfig>(
        VelocityQueueConfig::class.java,
        QueueVelocityFeature.dataDirectory,
        "velocity.yml",
    )
}
