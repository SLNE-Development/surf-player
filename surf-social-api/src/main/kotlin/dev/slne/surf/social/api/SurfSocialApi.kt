package dev.slne.surf.social.api

import dev.slne.surf.social.api.chat.SurfChatApi
import dev.slne.surf.social.api.connection.SurfConnectionApi
import dev.slne.surf.social.api.contentcreator.ContentCreatorApi
import dev.slne.surf.social.api.friends.player.FriendsPlayerManager
import dev.slne.surf.social.api.nametag.SurfNametagApi
import dev.slne.surf.social.api.tab.SurfTabApi

/**
 * Single entry point to every feature of surf-social.
 *
 * Each accessor resolves its service lazily, so a platform only needs to provide the services of
 * the features it actually uses (for example, the tab list is not available inside the
 * microservice).
 *
 * The feature specific entry points (e.g. [SurfChatApi.Companion]) remain available and resolve to
 * the same instances.
 */
object SurfSocialApi {

    /** Chat processing, message history and ignore lists. */
    val chat: SurfChatApi get() = SurfChatApi.INSTANCE

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
    val clans: SurfSocialClans get() = SurfSocialClans
}
