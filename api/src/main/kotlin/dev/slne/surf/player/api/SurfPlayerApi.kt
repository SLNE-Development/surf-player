package dev.slne.surf.player.api

import dev.slne.surf.player.api.chat.SurfChatApi
import dev.slne.surf.player.api.connection.SurfConnectionApi
import dev.slne.surf.player.api.contentcreator.SurfContentCreatorApi
import dev.slne.surf.player.api.clan.SurfClanApi
import dev.slne.surf.player.api.friends.SurfFriendsApi
import dev.slne.surf.player.api.minimap.SurfMinimapApi
import dev.slne.surf.player.api.nametag.SurfNametagApi
import dev.slne.surf.player.api.playtime.SurfPlaytimeApi
import dev.slne.surf.player.api.profile.SurfProfileApi
import dev.slne.surf.player.api.queue.SurfQueueApi
import dev.slne.surf.player.api.settings.SurfSettingsApi
import dev.slne.surf.player.api.tab.SurfTabApi
import dev.slne.surf.player.api.vanish.SurfVanishApi
import java.util.*

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

    /** The player with the given [uuid], with access to every feature. See [SurfCommunityPlayer]. */
    fun player(uuid: UUID): SurfCommunityPlayer = SurfCommunityPlayer(uuid)

    /** Per-player settings such as chat pings or direct messages (Paper, Minestom and Velocity). */
    val settings: SurfSettingsApi get() = SurfSettingsApi.INSTANCE

    /** The profile menu of players (Paper and Minestom). */
    val profile: SurfProfileApi get() = SurfProfileApi.INSTANCE

    /** The queues of players waiting to join a server, one per server. */
    val queue: SurfQueueApi get() = SurfQueueApi.INSTANCE

    /** Chat processing, message history and ignore lists. */
    val chat: SurfChatApi get() = SurfChatApi.INSTANCE

    /** Playtime sessions, totals and afk state of players (Paper and Minestom). */
    val playtime: SurfPlaytimeApi get() = SurfPlaytimeApi.INSTANCE

    /** Tab list placeholders and entry updates. */
    val tab: SurfTabApi get() = SurfTabApi.INSTANCE

    /** Friendships and friend requests. */
    val friends: SurfFriendsApi get() = SurfFriendsApi.INSTANCE

    /** Player nametags above the head. */
    val nametags: SurfNametagApi get() = SurfNametagApi.INSTANCE

    /** Content creators and their live state (e.g. Twitch). */
    val contentCreators: SurfContentCreatorApi get() = SurfContentCreatorApi.INSTANCE

    /** Linked Discord/Twitch accounts of players. */
    val connections: SurfConnectionApi get() = SurfConnectionApi.INSTANCE

    /** Clans and their members and invites. */
    val clans: SurfClanApi get() = SurfClanApi.INSTANCE

    /** Vanished players in the network (Paper only). */
    val vanish: SurfVanishApi get() = SurfVanishApi.INSTANCE

    /** Level IDs the minimap mods (Xaero, JourneyMap, VoxelMap) see (Paper and Minestom). */
    val minimap: SurfMinimapApi get() = SurfMinimapApi.INSTANCE
}
