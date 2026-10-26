package dev.slne.surf.minimapcompanion.core.service

import dev.slne.surf.minimapcompanion.core.config.CompanionConfig
import dev.slne.surf.minimapcompanion.core.config.MinimapCompanionConfig
import dev.slne.surf.minimapcompanion.core.id.LevelIds
import dev.slne.surf.minimapcompanion.core.platform.CompanionPlayer
import dev.slne.surf.minimapcompanion.core.xaero.XaeroChannels
import dev.slne.surf.minimapcompanion.core.xaero.XaeroLibPackets
import dev.slne.surf.minimapcompanion.core.xaero.XaeroPackets
import dev.slne.surf.minimapcompanion.core.xaero.XaeroServerProfile
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.seconds

/**
 * Sends players the Xaero data for the world they are in, as a server with Xaero's mods would on every world join.
 */
object XaeroSyncService {

    /**
     * How long after a join the platforms sync the player again, in case the client was not ready.
     */
    val JOIN_RESEND_DELAYS = listOf(1.seconds, 3.seconds)

    private val lastSentLevelIds = ConcurrentHashMap<UUID, Int>()

    /**
     * Sends [player] the Xaero data of their current world on each of [channels]: handshake, rules and level ID
     * on the map channels, then the enforced config through Xaero's Lib.
     */
    fun sync(
        player: CompanionPlayer,
        channels: Collection<String> = XaeroChannels.ALL,
        config: MinimapCompanionConfig = CompanionConfig.getConfig(),
    ) {
        val profile = XaeroServerProfile.of(config)
        val levelId = LevelIds.resolve(player.worldKey, player.worldIdentity, config)
        val levelIdPayload = XaeroPackets.levelId(levelId)

        val mapChannels = channels.filter(XaeroChannels::isMap)
        for (channel in mapChannels) {
            val rules = if (channel == XaeroChannels.MINIMAP) profile.minimapRules() else profile.worldMapRules()
            player.sendPluginMessage(channel, XaeroPackets.handshake())
            player.sendPluginMessage(channel, rules)
            player.sendPluginMessage(channel, levelIdPayload)
        }

        if (XaeroChannels.LIB in channels) {
            syncLib(player, profile)
        }

        if (mapChannels.isNotEmpty()) {
            lastSentLevelIds[player.uuid] = levelId
        }
    }

    // Resent on every world join: the client drops the enforced config whenever a new world
    // reaches it before the dimension handshake does.
    private fun syncLib(player: CompanionPlayer, profile: XaeroServerProfile) {
        player.sendPluginMessage(XaeroChannels.LIB, XaeroLibPackets.dimensionHandshake())
        player.sendPluginMessage(XaeroChannels.LIB, XaeroLibPackets.serverHandshake())

        for ((channel, options) in profile.configChannels()) {
            // A client without the mod would log the unknown config channel.
            if (channel != XaeroChannels.LIB && !player.hasChannel(channel)) continue

            player.sendPluginMessage(XaeroChannels.LIB, XaeroLibPackets.configChannel(channel))
            player.sendPluginMessage(XaeroChannels.LIB, XaeroLibPackets.profileInfoReset())
            player.sendPluginMessage(XaeroChannels.LIB, XaeroLibPackets.enforcedConfig(options.toNbt(), reset = true))
        }
    }

    fun lastSentLevelId(playerUuid: UUID): Int? = lastSentLevelIds[playerUuid]

    fun forget(playerUuid: UUID) {
        lastSentLevelIds.remove(playerUuid)
    }
}
