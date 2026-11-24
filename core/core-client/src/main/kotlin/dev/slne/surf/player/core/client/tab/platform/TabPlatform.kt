package dev.slne.surf.player.core.client.tab.platform

import dev.slne.surf.api.core.util.requiredService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import java.nio.file.Path
import java.util.*

private val platform = requiredService<TabPlatform>()

/**
 * The platform specific calls the shared tablist logic depends on.
 */
interface TabPlatform {

    /**
     * The directory this plugin keeps its files in.
     */
    val dataPath: Path

    fun onlinePlayers(): Collection<TabPlayer>

    fun onlinePlayerCount(): Int

    fun player(playerUuid: UUID): TabPlayer?

    fun maxPlayerCount(): Int

    fun isVanished(playerUuid: UUID): Boolean

    /**
     * Launches [block] on the scope the platform applies tablist changes on.
     */
    fun launch(block: suspend CoroutineScope.() -> Unit): Job

    companion object : TabPlatform by platform
}
