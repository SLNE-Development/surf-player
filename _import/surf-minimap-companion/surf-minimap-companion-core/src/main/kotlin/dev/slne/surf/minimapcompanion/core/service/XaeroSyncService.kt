package dev.slne.surf.minimapcompanion.core.service

import dev.slne.surf.minimapcompanion.core.config.CompanionConfig
import dev.slne.surf.minimapcompanion.core.config.MinimapCompanionConfig
import dev.slne.surf.minimapcompanion.core.id.LevelIds
import dev.slne.surf.minimapcompanion.core.platform.CompanionPlayer
import dev.slne.surf.minimapcompanion.core.xaero.XaeroChannels
import dev.slne.surf.minimapcompanion.core.xaero.XaeroPackets
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds

/**
 * Sends players the Xaero data for the world they are in.
 */
object XaeroSyncService {

    /**
     * How long after a join the platforms sync the player again, in case the client was not ready.
     */
    val JOIN_RESEND_DELAYS = listOf(1.seconds, 3.seconds)

    private val lastSentLevelIds = ConcurrentHashMap<UUID, Int>()

    /**
     * Sends [player] the level ID of their current world on each of [channels].
     */
    fun sync(
        player: CompanionPlayer,
        channels: Collection<String> = XaeroChannels.ALL,
        config: MinimapCompanionConfig = CompanionConfig.getConfig(),
    ) {
        val levelId = LevelIds.resolve(player.worldKey, player.worldIdentity, config)
        val payload = XaeroPackets.levelId(levelId)

        for (channel in channels.filter(XaeroChannels::isMap)) {
            player.sendPluginMessage(channel, payload)
        }

        lastSentLevelIds[player.uuid] = levelId
    }

    fun lastSentLevelId(playerUuid: UUID): Int? = lastSentLevelIds[playerUuid]

    fun forget(playerUuid: UUID) {
        lastSentLevelIds.remove(playerUuid)
    }
}
