package dev.slne.surf.minimapcompanion.core.xaero

import dev.slne.surf.minimapcompanion.core.config.CompanionConfigs
import dev.slne.surf.minimapcompanion.core.config.MinimapCompanionConfig
import dev.slne.surf.minimapcompanion.core.config.XaeroConfig
import dev.slne.surf.minimapcompanion.core.global.GlobalSettings
import dev.slne.surf.minimapcompanion.core.platform.CompanionPlayer
import dev.slne.surf.minimapcompanion.core.world.LevelIds
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
     * on the map channels, then the enforced config through Xaero's Lib. While `disable-maps` is on, the maps
     * are hidden too.
     */
    fun sync(
        player: CompanionPlayer,
        channels: Collection<String> = XaeroChannels.ALL,
        config: MinimapCompanionConfig = CompanionConfigs.current.config,
        xaero: XaeroConfig = CompanionConfigs.current.xaero,
    ) {
        val profile = XaeroServerProfile.of(xaero, GlobalSettings.of(config.globalSettings))
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

        XaeroMapEffects.sync(player, config.disableMaps)
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
        XaeroMapEffects.forget(playerUuid)
    }
}
