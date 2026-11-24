package dev.slne.surf.player.api

import dev.slne.surf.player.api.chat.ChatCapability
import dev.slne.surf.player.api.clan.ClanCapability
import dev.slne.surf.player.api.connection.ConnectionCapability
import dev.slne.surf.player.api.contentcreator.ContentCreatorCapability
import dev.slne.surf.player.api.friends.FriendsCapability
import dev.slne.surf.player.api.minimap.MinimapCapability
import dev.slne.surf.player.api.nametag.NametagCapability
import dev.slne.surf.player.api.playtime.PlaytimeCapability
import dev.slne.surf.player.api.profile.ProfileCapability
import dev.slne.surf.player.api.queue.QueueCapability
import dev.slne.surf.player.api.settings.SettingsCapability
import dev.slne.surf.player.api.tab.TabCapability
import dev.slne.surf.player.api.vanish.VanishCapability
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
    ChatCapability,
    ClanCapability,
    ConnectionCapability,
    ContentCreatorCapability,
    FriendsCapability,
    MinimapCapability,
    NametagCapability,
    PlaytimeCapability,
    ProfileCapability,
    QueueCapability,
    SettingsCapability,
    TabCapability,
    VanishCapability
