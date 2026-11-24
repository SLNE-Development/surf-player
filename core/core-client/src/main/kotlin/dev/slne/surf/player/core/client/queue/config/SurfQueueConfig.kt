package dev.slne.surf.player.core.client.queue.config

import dev.slne.surf.api.core.config.SpongeYmlConfigClass
import dev.slne.surf.player.core.client.queue.ClientQueueInstance
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class SurfQueueConfig(
    val maxTransfersPerSecond: Int = 20,
) {
    companion object : SpongeYmlConfigClass<SurfQueueConfig>(
        SurfQueueConfig::class.java,
        ClientQueueInstance.get().dataPath,
        "config.yml"
    )
}
