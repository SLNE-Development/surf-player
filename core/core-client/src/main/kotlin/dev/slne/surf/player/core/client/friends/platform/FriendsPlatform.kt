package dev.slne.surf.player.core.client.friends.platform

import dev.slne.surf.api.core.util.requiredService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import java.util.*

/**
 * The platform-specific operations the shared friends logic relies on.
 *
 * Every platform contributes exactly one implementation through `ServiceLoader`.
 */
interface FriendsPlatform {

    /**
     * Whether the player identified by [uuid] is connected to this server.
     */
    fun isOnline(uuid: UUID): Boolean

    /**
     * Sends [message] to the player identified by [uuid], if they are connected to this server.
     */
    fun sendMessage(uuid: UUID, message: Component)

    /**
     * Plays [sound] to the player identified by [uuid], if they are connected to this server.
     */
    fun playSound(uuid: UUID, sound: Sound)

    /**
     * Launches [block] on the main dispatcher of the platform.
     */
    fun launch(block: suspend CoroutineScope.() -> Unit): Job

    companion object {
        val INSTANCE get() = platform
    }
}

private val platform = requiredService<FriendsPlatform>()
