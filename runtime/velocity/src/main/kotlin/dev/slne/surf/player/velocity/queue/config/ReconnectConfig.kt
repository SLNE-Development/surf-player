package dev.slne.surf.player.velocity.queue.config

import dev.slne.surf.api.core.config.type.ConfigDuration
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment
import kotlin.time.Duration.Companion.seconds

/**
 * Requeueing players for the server they were on after they lost their connection.
 */
@ConfigSerializable
data class ReconnectConfig(
    @param:Comment(
        "Whether reconnect queueing is enabled."
    )
    val enabled: Boolean = true,

    @param:Comment(
        "How long, a reconnect entry remains valid."
    )
    val timeout: ConfigDuration = ConfigDuration(120.seconds),

    @param:Comment(
        "Servers for which reconnect queueing is available."
    )
    val servers: List<String> = listOf("survival"),
) {

    fun timeoutDuration() = timeout.asDuration()

    fun isEnabledFor(serverName: String): Boolean {
        return enabled && serverName in servers
    }
}
