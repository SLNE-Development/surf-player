package dev.slne.surf.player.core.client.playtime.platform

import dev.slne.surf.api.core.util.requiredService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import net.kyori.adventure.audience.Audience
import java.util.*

private val platform = requiredService<PlaytimePlatform>()

/**
 * The platform specific calls the shared playtime logic depends on.
 */
interface PlaytimePlatform {
    /**
     * Whether the optional surf-transaction plugin is available to pay out paychecks.
     */
    val transactionAvailable: Boolean

    /**
     * Returns the online player with the given [playerUuid] as an [Audience], or `null` if they
     * are not online.
     */
    fun onlinePlayer(playerUuid: UUID): Audience?

    /**
     * Launches [block] on the platform's asynchronous scope.
     */
    fun launchAsync(block: suspend CoroutineScope.() -> Unit): Job

    companion object : PlaytimePlatform by platform
}
