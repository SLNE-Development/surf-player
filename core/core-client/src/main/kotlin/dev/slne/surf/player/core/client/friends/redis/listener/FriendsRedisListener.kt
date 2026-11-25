package dev.slne.surf.player.core.client.friends.redis.listener

import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.clickRunsCommand
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.util.sendText
import dev.slne.surf.player.api.friends.SurfFriendsApi
import dev.slne.surf.player.api.friends.utils.displayName
import dev.slne.surf.player.api.friends.utils.toSurfPlayer
import dev.slne.surf.player.core.client.friends.connection.FriendConnectionNotifier
import dev.slne.surf.player.core.client.friends.platform.FriendsPlatform
import dev.slne.surf.player.core.client.friends.util.resolveUsername
import dev.slne.surf.player.core.client.friends.redis.event.FriendsAction
import dev.slne.surf.player.core.client.friends.redis.event.FriendsRedisEvent
import dev.slne.surf.redis.event.OnRedisEvent
import net.kyori.adventure.text.event.HoverEvent
import java.util.*

/**
 * Notifies the players on this server about the friends actions of [FriendsRedisEvent].
 */
object FriendsRedisListener {
    @OnRedisEvent
    fun onFriendsEvent(event: FriendsRedisEvent) = FriendsPlatform.INSTANCE.launch {
        val executor = event.executorUuid.toSurfPlayer()?.takeIf(::isOnThisServer)
        val target = event.targetUuid.toSurfPlayer()?.takeIf(::isOnThisServer)

        if (executor == null && target == null) return@launch

        when (event.action) {
            FriendsAction.REQUEST_SENT -> onRequestSent(event, executor, target)
            FriendsAction.REQUEST_REVOKED -> onRequestRevoked(event, executor, target)
            FriendsAction.REQUEST_ACCEPTED -> onRequestAccepted(event, executor, target)
            FriendsAction.REQUEST_DECLINED -> onRequestDeclined(event, executor, target)
            FriendsAction.FRIENDSHIP_REMOVED -> onFriendshipRemoved(event, executor, target)
        }
    }

    private suspend fun onRequestAccepted(event: FriendsRedisEvent, executor: SurfPlayer?, target: SurfPlayer?) {
        target?.sendText {
            appendInfoPrefix()

            append(displayNameOf(event.executorUuid))
            info(" hat deine Freundschaftsanfrage angenommen.")
        }

        if (target != null && SurfFriendsApi.friendSoundsEnabled(target.uuid)) {
            FriendsPlatform.INSTANCE.playSound(target.uuid, FriendConnectionNotifier.ONLINE_SOUND)
        }

        executor?.sendText {
            appendSuccessPrefix()

            success("Du hast die Freundschaftsanfrage von ")
            append(displayNameOf(event.targetUuid))
            success(" angenommen.")
        }
    }

    private suspend fun onRequestDeclined(event: FriendsRedisEvent, executor: SurfPlayer?, target: SurfPlayer?) {
        target?.sendText {
            appendInfoPrefix()

            append(displayNameOf(event.executorUuid))
            info(" hat deine Freundschaftsanfrage abgelehnt.")
        }

        executor?.sendText {
            appendSuccessPrefix()

            success("Du hast die Freundschaftsanfrage von ")
            append(displayNameOf(event.targetUuid))
            success(" abgelehnt.")
        }
    }

    private suspend fun onRequestRevoked(event: FriendsRedisEvent, executor: SurfPlayer?, target: SurfPlayer?) {
        target?.sendText {
            appendInfoPrefix()

            info("Die Freundschaftsanfrage von ")
            append(displayNameOf(event.executorUuid))
            info(" wurde zurückgezogen.")
        }

        executor?.sendText {
            appendSuccessPrefix()

            success("Du hast die Freundschaftsanfrage an ")
            append(displayNameOf(event.targetUuid))
            success(" zurückgezogen.")
        }
    }

    /**
     * The target is only notified if they enabled friend request notifications. This is checked
     * here, on the server hosting the target, because only that server has their settings cached.
     */
    private suspend fun onRequestSent(event: FriendsRedisEvent, executor: SurfPlayer?, target: SurfPlayer?) {
        val executorUuid = event.executorUuid
        val targetUuid = event.targetUuid

        if (target != null && SurfFriendsApi.friendRequestNotificationsEnabled(targetUuid)) {
            val executorName = displayNameOf(executorUuid)
            // The buttons run the commands, which find the request by the sender's name.
            val executorCommandName = resolveUsername(executorUuid) ?: executorUuid.toString()

            if (SurfFriendsApi.friendSoundsEnabled(targetUuid)) {
                FriendsPlatform.INSTANCE.playSound(targetUuid, FriendConnectionNotifier.REQUEST_SOUND)
            }

            target.sendText {
                appendInfoPrefix()

                info("Du hast eine Freundschaftsanfrage von ")
                append(executorName)
                info(" erhalten.")
                appendSpace()
                append {
                    success("[✔]")
                    hoverEvent(HoverEvent.showText(buildText {
                        spacer("Klicke, um die Freundschaftsanfrage von ")
                        append(executorName)
                        spacer(" anzunehmen.")
                    }))
                    clickRunsCommand("/friend accept $executorCommandName")
                }
                appendSpace()
                append {
                    error("[❌]")
                    hoverEvent(HoverEvent.showText(buildText {
                        spacer("Klicke, um die Freundschaftsanfrage von ")
                        append(executorName)
                        spacer(" abzulehnen.")
                    }))
                    clickRunsCommand("/friend deny $executorCommandName")
                }
            }
        }

        if (executor != null) {
            val targetPlayer = targetUuid.toSurfPlayer()

            executor.sendText {
                appendSuccessPrefix()
                success("Du hast eine Freundschaftsanfrage an ")
                append(targetPlayer.displayName())
                success(" gesendet.")
                append {
                    clickRunsCommand("/friend revoke ${targetPlayer?.username}")
                    spacer(" [")
                    info("Zurückziehen".toSmallCaps())
                    spacer("]")
                }
            }
        }
    }

    private suspend fun onFriendshipRemoved(event: FriendsRedisEvent, executor: SurfPlayer?, target: SurfPlayer?) {
        target?.sendText {
            appendInfoPrefix()

            append(displayNameOf(event.executorUuid))
            info(" hat dich als Freund entfernt.")
        }

        executor?.sendText {
            appendSuccessPrefix()

            success("Du hast ")
            append(displayNameOf(event.targetUuid))
            success(" als Freund entfernt.")
        }
    }

    private suspend fun displayNameOf(uuid: UUID) = uuid.toSurfPlayer().displayName()

    private fun isOnThisServer(surfPlayer: SurfPlayer) = FriendsPlatform.INSTANCE.isOnline(surfPlayer.uuid)
}
