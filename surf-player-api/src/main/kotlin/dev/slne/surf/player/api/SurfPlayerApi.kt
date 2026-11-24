package dev.slne.surf.player.api

import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.player.api.chat.SurfChatApi
import dev.slne.surf.player.api.connection.SurfConnectionApi
import dev.slne.surf.player.api.contentcreator.ContentCreatorApi
import dev.slne.surf.player.api.friends.player.FriendsPlayerManager
import dev.slne.surf.player.api.minimap.SurfMinimapCompanionApi
import dev.slne.surf.player.api.nametag.SurfNametagApi
import dev.slne.surf.player.api.playtime.SurfPlaytimeApi
import dev.slne.surf.player.api.profile.SurfProfileApi
import dev.slne.surf.player.api.queue.SurfQueue
import dev.slne.surf.player.api.settings.SurfSettingsApi
import dev.slne.surf.player.api.tab.SurfTabApi
import dev.slne.surf.player.api.vanish.SurfVanishApi

/**
 * Single entry point to every feature of surf-player.
 *
 * Each accessor resolves its service lazily, so a platform only needs to provide the services of
 * the features it actually uses (for example, the tab list is not available inside the
 * microservice).
 *
 * The feature specific entry points (e.g. [SurfChatApi.Companion]) remain available and resolve to
 * the same instances.
 */
object SurfPlayerApi {

    /** Per-player settings such as chat pings or direct messages (Paper, Minestom and Velocity). */
    val settings: SurfSettingsApi get() = SurfSettingsApi

    /** The profile menu of players (Paper and Minestom). */
    val profile: SurfProfileApi get() = SurfProfileApi.INSTANCE

    /** The queue of players waiting to join the server named [serverName]. */
    fun queue(serverName: String): SurfQueue = SurfQueue.byServer(serverName)

    /** The queue of players waiting to join [server]. */
    fun queue(server: SurfServer): SurfQueue = SurfQueue.byServer(server)

    /** Chat processing, message history and ignore lists. */
    val chat: SurfChatApi get() = SurfChatApi.INSTANCE

    /** Playtime sessions, totals and afk state of players (Paper and Minestom). */
    val playtime: SurfPlaytimeApi get() = SurfPlaytimeApi.INSTANCE

    /** Tab list placeholders and entry updates. */
    val tab: SurfTabApi get() = SurfTabApi.INSTANCE

    /** Friendships and friend requests, resolved per player. */
    val friends: FriendsPlayerManager get() = FriendsPlayerManager.INSTANCE

    /** Player nametags above the head. */
    val nametags: SurfNametagApi get() = SurfNametagApi.INSTANCE

    /** Content creators and their live state (e.g. Twitch). */
    val contentCreators: ContentCreatorApi get() = ContentCreatorApi.INSTANCE

    /** Linked Discord/Twitch accounts of players. */
    val connections: SurfConnectionApi get() = SurfConnectionApi.INSTANCE

    /** Access to clans and their members, invites and players. */
    val clans: SurfPlayerClans get() = SurfPlayerClans

    /** Vanished players in the network (Paper only). */
    val vanish: SurfVanishApi get() = SurfVanishApi.INSTANCE

    /** Level IDs the minimap mods (Xaero, JourneyMap, VoxelMap) see (Paper and Minestom). */
    val minimap: SurfMinimapCompanionApi get() = SurfMinimapCompanionApi.INSTANCE
}
