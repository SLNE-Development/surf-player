package dev.slne.surf.player.api

import dev.slne.surf.player.api.chat.HasChat
import dev.slne.surf.player.api.clan.HasClan
import dev.slne.surf.player.api.connection.HasConnections
import dev.slne.surf.player.api.contentcreator.HasContentCreator
import dev.slne.surf.player.api.friends.HasFriends
import dev.slne.surf.player.api.minimap.HasMinimap
import dev.slne.surf.player.api.nametag.HasNametag
import dev.slne.surf.player.api.playtime.HasPlaytime
import dev.slne.surf.player.api.profile.HasProfile
import dev.slne.surf.player.api.queue.HasQueue
import dev.slne.surf.player.api.settings.HasSettings
import dev.slne.surf.player.api.tab.HasTab
import dev.slne.surf.player.api.vanish.HasVanish
import java.util.*

/**
 * A player of the network, seen through every surf-player feature.
 *
 * This is a lightweight value class around the player's [uuid]; creating one is free and it is
 * never cached. Every capability delegates to the corresponding feature API, so a member works on
 * every platform that provides that feature.
 *
 * ```kotlin
 * val player = SurfPlayerApi.player(uuid)
 * player.clan()
 * player.isVanished
 * ```
 */
@JvmInline
value class SurfCommunityPlayer(override val uuid: UUID) :
    HasChat,
    HasClan,
    HasConnections,
    HasContentCreator,
    HasFriends,
    HasMinimap,
    HasNametag,
    HasPlaytime,
    HasProfile,
    HasQueue,
    HasSettings,
    HasTab,
    HasVanish
