package dev.slne.surf.player.core.client.friends.connection

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.clickRunsCommand
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.player.api.friends.SurfFriendsApi
import dev.slne.surf.player.api.friends.utils.displayName
import dev.slne.surf.player.api.friends.utils.toSurfPlayer
import dev.slne.surf.player.core.client.friends.FriendsClient
import dev.slne.surf.player.core.client.friends.platform.FriendsPlatform
import dev.slne.surf.player.core.client.friends.redis.event.FriendConnectionRedisEvent
import dev.slne.surf.player.core.client.vanish.PlayerVisibility
import dev.slne.surf.player.core.client.vanish.VanishedPlayers
import dev.slne.surf.redis.event.OnRedisEvent
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Announces players joining and leaving the network to their friends on every server.
 *
 * The proxy publishes [FriendConnectionRedisEvent] once per network join and quit; server switches
 * publish nothing, so friends are never notified twice. Every server notifies the friends it hosts,
 * respecting their friend notification and sound settings.
 *
 * The first server a player joins also greets them. The proxy event and the local join can arrive
 * in either order, so both are recorded and the greeting is sent once both are there.
 */
object FriendConnectionNotifier {
    /** Played to friends with friend sounds enabled when a friend comes online. */
    val ONLINE_SOUND: Sound = Sound.sound(Key.key("entity.experience_orb.pickup"), Sound.Source.MASTER, 0.6f, 1.4f)

    /** Played to friends with friend sounds enabled when a friend goes offline. */
    val OFFLINE_SOUND: Sound = Sound.sound(Key.key("block.note_block.bass"), Sound.Source.MASTER, 0.6f, 0.8f)

    /** Played to players with friend sounds enabled when they receive a friend request. */
    val REQUEST_SOUND: Sound = Sound.sound(Key.key("block.note_block.pling"), Sound.Source.MASTER, 0.6f, 1.6f)

    private val pendingGreetings: Cache<UUID, GreetingState> = Caffeine.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES)
        .build()

    private data class GreetingState(val joinedHere: Boolean, val cameOnline: Boolean)

    /**
     * Records that [uuid] joined this server; greets them if the proxy already reported this as
     * their network join.
     */
    fun onJoinedThisServer(uuid: UUID) {
        if (recordGreeting(uuid, joinedHere = true)) greet(uuid)
    }

    @OnRedisEvent
    fun onFriendConnection(event: FriendConnectionRedisEvent) {
        val uuid = event.playerUuid

        // Vanished players stay hidden: only friends who may see them (PlayerVisibility) get a
        // notice. They are still greeted below. The vanish state is read when the event arrives.
        val targetPriority = VanishedPlayers.vanishPriority(uuid)
        val visibility = PlayerVisibility { if (it == uuid) targetPriority else null }

        FriendsPlatform.INSTANCE.launch {
            if (event.online) {
                announce(uuid, " ist nun online.", ONLINE_SOUND, visibility)
            } else {
                announce(uuid, " ist nun offline.", OFFLINE_SOUND, visibility)
            }
        }

        if (event.online && event.serverName == SurfServer.current().name) {
            if (recordGreeting(uuid, joinedHere = false)) {
                FriendsPlatform.INSTANCE.launch { greet(uuid) }
            }
        }
    }

    /**
     * Records one half of the greeting handshake atomically; `true` once both halves are there,
     * which happens exactly once per network join.
     */
    private fun recordGreeting(uuid: UUID, joinedHere: Boolean): Boolean {
        var complete = false

        pendingGreetings.asMap().compute(uuid) { _, state ->
            val joined = joinedHere || state?.joinedHere == true
            val cameOnline = !joinedHere || state?.cameOnline == true

            if (joined && cameOnline) {
                complete = true
                null
            } else {
                GreetingState(joined, cameOnline)
            }
        }

        return complete
    }

    /**
     * Greets [uuid] with how many of their friends are online on the network (vanished friends they
     * may not see are not counted) and how many friend requests are still open.
     */
    private fun greet(uuid: UUID) {
        val platform = FriendsPlatform.INSTANCE
        val viewer = platform.viewer(uuid) ?: return

        val openFriendRequests = countReceivedFriendRequests(uuid)
        val onlineFriends = SurfFriendsApi.onlineFriendUuids(uuid).count { PlayerVisibility.network.canSee(viewer, it) }

        if (onlineFriends > 0) {
            platform.sendMessage(uuid, buildText {
                appendInfoPrefix()
                info("Aktuell sind ")
                variableValue(onlineFriends)
                info(" deiner Freunde online. ")

                append {
                    clickRunsCommand("/friend list")
                    info("[Ansehen]".toSmallCaps())
                    hoverEvent(buildText {
                        info("Klicke hier, um deine Freunde anzusehen.")
                    })
                }
            })
        }

        if (openFriendRequests > 0) {
            platform.sendMessage(uuid, buildText {
                appendInfoPrefix()
                info("Du hast noch ")
                variableValue(openFriendRequests)
                info(" Freundschaftsanfragen offen. ")

                append {
                    clickRunsCommand("/friend requests")
                    info("[Ansehen]".toSmallCaps())
                    hoverEvent(buildText {
                        info("Klicke hier, um deine Freundschaftsanfragen anzusehen.")
                    })
                }
            })
        }
    }

    /**
     * Tells the friends of [uuid] on this server [message], if they enabled friend notifications
     * and may see [uuid] according to [visibility], and plays [sound] to those who also enabled
     * friend sounds.
     */
    private suspend fun announce(uuid: UUID, message: String, sound: Sound, visibility: PlayerVisibility) {
        val friendUuids = friendsOnThisServer(uuid)
        if (friendUuids.isEmpty()) return

        val announcement = buildText {
            appendInfoPrefix()
            append(uuid.toSurfPlayer().displayName())
            info(message)
        }
        val platform = FriendsPlatform.INSTANCE

        for (friendUuid in friendUuids) {
            val viewer = platform.viewer(friendUuid) ?: continue
            if (!visibility.canSee(viewer, uuid)) continue
            if (!SurfFriendsApi.friendNotificationsEnabled(friendUuid)) continue

            platform.sendMessage(friendUuid, announcement)

            if (SurfFriendsApi.friendSoundsEnabled(friendUuid)) {
                platform.playSound(friendUuid, sound)
            }
        }
    }

    /**
     * How many friend requests [uuid] has received.
     */
    private fun countReceivedFriendRequests(uuid: UUID): Int {
        val snapshot = FriendsClient.friendRequests.snapshot()
        var count = 0

        for (index in snapshot.indices) {
            if (snapshot[index].targetUuid == uuid) count++
        }

        return count
    }

    /**
     * The friends of [uuid] that are connected to this server.
     */
    private fun friendsOnThisServer(uuid: UUID): List<UUID> {
        val snapshot = FriendsClient.friendships.snapshot()
        val platform = FriendsPlatform.INSTANCE
        val online = ObjectArrayList<UUID>()

        for (index in snapshot.indices) {
            val friendship = snapshot[index]
            if (friendship.playerUuid != uuid) continue
            if (!platform.isOnline(friendship.friendUuid)) continue

            online.add(friendship.friendUuid)
        }

        return online
    }
}
