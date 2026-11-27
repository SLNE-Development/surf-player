package dev.slne.surf.player.core.client.profile.menu.friends

import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.util.dateTimeFormatter
import dev.slne.surf.core.api.common.SurfCoreApi
import dev.slne.surf.player.api.SurfCommunityPlayer
import dev.slne.surf.player.api.friends.SurfFriendsApi
import dev.slne.surf.player.api.friends.result.FriendRequestRemoveResult
import dev.slne.surf.player.core.client.command.runServiceAction
import dev.slne.surf.player.core.client.friends.command.NO_SENT_FRIEND_REQUEST_MESSAGE
import dev.slne.surf.player.core.client.friends.command.handleFriendRemoveClick
import dev.slne.surf.player.core.client.friends.friendRequestAnswerFailureMessage
import dev.slne.surf.player.core.client.friends.platform.FriendsPlatform
import dev.slne.surf.player.core.client.friends.util.format
import dev.slne.surf.player.core.client.friends.util.resolveUsername
import dev.slne.surf.player.core.client.profile.menu.own.localColored
import dev.slne.surf.player.core.client.vanish.PlayerVisibility
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextDecoration
import java.time.OffsetDateTime
import java.util.*

/**
 * A friend as the friends menu shows them: the details of `/friend info` plus the status.
 * [server] is only set while the friend is [online]
 * (and not hidden from the viewer by vanish).
 */
data class FriendMenuEntry(
    val uuid: UUID,
    val name: String,
    val online: Boolean,
    val server: String?,
    val friendsSince: OffsetDateTime,
    val firstSeen: String?,
)

/** A pending friend request the viewer received, as the friends menu shows it. */
data class FriendRequestMenuEntry(
    val senderUuid: UUID,
    val name: String,
    val createdAt: OffsetDateTime,
)

/** A pending friend request the viewer sent to [targetUuid], as the friends menu shows it. */
data class SentFriendRequestMenuEntry(
    val targetUuid: UUID,
    val name: String,
    val createdAt: OffsetDateTime,
)

/**
 * The platform-independent data and texts of the friends menus (friend list and received
 * requests), opened from the own-profile menu.
 *
 * Platform modules supply the inventory items and the click handling and take their display
 * names and lore from here, so both platforms render the same menus.
 */
object FriendsMenuContent {

    const val TITLE = "Freunde"
    const val REQUESTS_TITLE = "Freundschaftsanfragen"
    const val SENT_REQUESTS_TITLE = "Gesendete Anfragen"

    private const val UNKNOWN_SERVER = "Unbekannt"
    private const val UNKNOWN = "Unbekannt"

    /**
     * The friends of [player], online ones first and each group sorted by name. Friends who are
     * vanished and whom [player] may not see (see [PlayerVisibility.canSee]) are listed as offline,
     * exactly like in `/friend list`.
     */
    suspend fun friends(player: UUID): List<FriendMenuEntry> {
        val viewer = FriendsPlatform.INSTANCE.viewer(player)

        val entries = SurfFriendsApi.friendships(player).map { friendship ->
            val friendUuid = friendship.friendUuid
            val visibleFriend = SurfCoreApi.getPlayer(friendUuid)
                ?.takeIf { viewer != null && PlayerVisibility.network.canSee(viewer, friendUuid) }

            FriendMenuEntry(
                uuid = friendUuid,
                // Never leave a friend out because their name is unknown to the lookup service.
                name = resolveUsername(friendUuid) ?: friendUuid.toString(),
                online = visibleFriend != null,
                server = visibleFriend?.let { it.currentServer?.displayName ?: UNKNOWN_SERVER },
                friendsSince = friendship.createdAt,
                firstSeen = (visibleFriend ?: SurfCoreApi.getOfflinePlayer(friendUuid))
                    ?.firstSeen?.format(dateTimeFormatter),
            )
        }

        return entries.sortedWith(compareByDescending<FriendMenuEntry> { it.online }.thenBy { it.name.lowercase() })
    }

    /** The pending friend requests [player] received, newest first. */
    suspend fun requests(player: UUID): List<FriendRequestMenuEntry> =
        SurfFriendsApi.receivedFriendRequests(player)
            .sortedByDescending { it.createdAt }
            .map {
                FriendRequestMenuEntry(
                    senderUuid = it.senderUuid,
                    name = resolveUsername(it.senderUuid) ?: it.senderUuid.toString(),
                    createdAt = it.createdAt,
                )
            }

    /** The pending friend requests [player] sent, newest first. */
    suspend fun sentRequests(player: UUID): List<SentFriendRequestMenuEntry> =
        SurfFriendsApi.sentFriendRequests(player)
            .sortedByDescending { it.createdAt }
            .map {
                SentFriendRequestMenuEntry(
                    targetUuid = it.targetUuid,
                    name = resolveUsername(it.targetUuid) ?: it.targetUuid.toString(),
                    createdAt = it.createdAt,
                )
            }

    /**
     * Revokes the request [player] sent to [target], as `/friend revoke` does. [audience] hears
     * about it if the request does not exist (any more) or the service cannot be reached.
     */
    suspend fun revokeRequest(player: UUID, target: UUID, audience: Audience) {
        runServiceAction(audience) {
            val result = SurfCommunityPlayer(player).revokeFriendRequest(target)
            if (result !is FriendRequestRemoveResult.Success) audience.sendMessage(NO_SENT_FRIEND_REQUEST_MESSAGE)
        }
    }

    /**
     * Accepts ([accept]) or declines the request [player] received from [sender], as the command
     * does. The confirmation of a success is sent by the friends event; [audience] only hears about
     * a failure.
     */
    suspend fun answerRequest(player: UUID, sender: UUID, accept: Boolean, audience: Audience) {
        runServiceAction(audience) {
            val me = SurfCommunityPlayer(player)
            val result = if (accept) me.acceptFriendRequest(sender) else me.declineFriendRequest(sender)
            friendRequestAnswerFailureMessage(result)?.let(audience::sendMessage)
        }
    }

    /** The display name of the item of [entry]. */
    fun friendDisplayName(entry: FriendMenuEntry): Component = buildText {
        localColored(entry.name.toSmallCaps(), TextDecoration.BOLD)
    }

    /** The lore of the item of [entry]: status, friendship since and first login (as `/friend info`). */
    fun friendLore(entry: FriendMenuEntry): Array<TextComponent> = arrayOf(
        buildText { },
        buildText { variableValue("Status:".toSmallCaps()) },
        buildText {
            if (entry.online) success("Online auf ${entry.server}") else error("Offline")
        },
        buildText { },
        buildText { variableValue("Befreundet seit:".toSmallCaps()) },
        buildText { note(entry.friendsSince.format()) },
        buildText { },
        buildText { variableValue("Erster Login:".toSmallCaps()) },
        buildText { note(entry.firstSeen ?: UNKNOWN) },
    )

    /**
     * Ends the friendship of [player] with [friend], as `/friend remove` does after its
     * confirmation. [audience] hears about it if they are no longer friends or the service cannot
     * be reached.
     */
    suspend fun removeFriend(player: UUID, friend: UUID, audience: Audience) {
        runServiceAction(audience) { handleFriendRemoveClick(audience, player, friend) }
    }

    /** The title of the menu of a single friend. */
    const val FRIEND_TITLE = "Freund"

    /** The title of the menu that asks to confirm ending a friendship. */
    const val REMOVE_TITLE = "Freundschaft beenden?"

    /** The lore of the item of [entry] in their own menu: the same details as in the list. */
    fun friendDetailLore(entry: FriendMenuEntry): Array<TextComponent> = friendLore(entry)

    val removeButtonDisplayName: Component = buildText {
        error("Freundschaft beenden".toSmallCaps(), TextDecoration.BOLD)
    }

    val removeButtonLore: Array<TextComponent> = arrayOf(
        buildText { },
        buildText { spacer("Klicke, um die Freundschaft zu beenden.") },
        buildText { spacer("Du wirst vorher um eine Bestätigung gebeten.") },
    )

    /** The display name of the item that asks whether to end the friendship with [entry]. */
    fun removeQuestionDisplayName(entry: FriendMenuEntry): Component = buildText {
        warning("Möchtest du die Freundschaft mit ")
        variableValue(entry.name)
        warning(" wirklich beenden?")
    }

    val removeQuestionLore: Array<TextComponent> = arrayOf(
        buildText { },
        buildText { spacer("Um wieder befreundet zu sein, braucht ihr eine neue Freundschaftsanfrage.") },
    )

    val removeConfirmDisplayName: Component = buildText {
        error("Ja, Freundschaft beenden".toSmallCaps(), TextDecoration.BOLD)
    }

    val removeCancelDisplayName: Component = buildText {
        success("Abbrechen".toSmallCaps(), TextDecoration.BOLD)
    }

    /** The display name of the item of the request [entry]. */
    fun requestDisplayName(entry: FriendRequestMenuEntry): Component = buildText {
        localColored(entry.name.toSmallCaps(), TextDecoration.BOLD)
    }

    /** The lore of the item of the request [entry]: when it was sent and how to answer it. */
    fun requestLore(entry: FriendRequestMenuEntry): Array<TextComponent> = arrayOf(
        buildText { },
        buildText { variableValue("Gesendet:".toSmallCaps()) },
        buildText { note(entry.createdAt.format()) },
        buildText { },
        buildText { spacer("Linksklick, um die Anfrage anzunehmen.") },
        buildText { spacer("Rechtsklick, um die Anfrage abzulehnen.") },
    )

    /** The display name of the button that opens the received requests; [count] are pending. */
    fun requestsButtonDisplayName(count: Int): Component = buildText {
        localColored("Anfragen".toSmallCaps(), TextDecoration.BOLD)
        appendSpace()
        spacer("($count)")
    }

    val requestsButtonLore: Array<TextComponent> = arrayOf(
        buildText { },
        buildText { spacer("Klicke, um deine offenen Freundschaftsanfragen zu sehen.") },
    )

    /** The display name of the item of the sent request [entry]. */
    fun sentRequestDisplayName(entry: SentFriendRequestMenuEntry): Component = buildText {
        localColored(entry.name.toSmallCaps(), TextDecoration.BOLD)
    }

    /** The lore of the item of the sent request [entry]: when it was sent and how to revoke it. */
    fun sentRequestLore(entry: SentFriendRequestMenuEntry): Array<TextComponent> = arrayOf(
        buildText { },
        buildText { variableValue("Gesendet:".toSmallCaps()) },
        buildText { note(entry.createdAt.format()) },
        buildText { },
        buildText { spacer("Klicke, um die Anfrage zurückzuziehen.") },
    )

    /** The display name of the button that opens the sent requests; [count] are pending. */
    fun sentRequestsButtonDisplayName(count: Int): Component = buildText {
        localColored("Gesendet".toSmallCaps(), TextDecoration.BOLD)
        appendSpace()
        spacer("($count)")
    }

    val sentRequestsButtonLore: Array<TextComponent> = arrayOf(
        buildText { },
        buildText { spacer("Klicke, um deine gesendeten Freundschaftsanfragen zu sehen.") },
    )

    val backDisplayName: Component = buildText { error("Zurück") }

    val noFriends: Array<TextComponent> = arrayOf(
        buildText { info("Du hast noch keine Freunde.") },
        buildText { spacer("Sende eine Anfrage mit /friend add <Spieler>.") },
    )
}
