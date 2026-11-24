package dev.slne.surf.player.core.client.friends.command

import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.CommonComponents
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.pagination.Pagination
import dev.slne.surf.player.api.friends.SurfFriendsApi
import dev.slne.surf.player.api.friends.model.FriendRequest
import dev.slne.surf.player.core.client.friends.util.format
import dev.slne.surf.player.core.client.friends.util.resolveUsername
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import java.time.OffsetDateTime
import java.util.*

private val pagination = Pagination<LocalFriendRequest> {
    title { primary("Offene Freundschaftsanfragen".toSmallCaps(), TextDecoration.BOLD) }

    rowRenderer { row, _ ->
        listOf(
            buildText {
                append(CommonComponents.EM_DASH)
                appendSpace()
                variableKey(row.requesterName)
                spacer(" (${row.createdAt.format()})")
                hoverEvent(buildText {
                    info("Klicke hier, um die Freundschaftsanfrage von ")
                    variableValue(row.requesterName)
                    info(" anzunehmen.")
                })
                clickRunsCommand("/friend accept ${row.requesterName}")
            }
        )
    }
}

internal data class LocalFriendRequest(
    val requesterName: String,
    val createdAt: OffsetDateTime
)

/**
 * One row per request of [requests], newest first. A sender whose name [nameOf] cannot resolve is
 * listed by their UUID instead of being left out, so a pending request never disappears from the
 * list.
 */
internal suspend fun friendRequestRows(
    requests: Collection<FriendRequest>,
    nameOf: suspend (UUID) -> String?,
): List<LocalFriendRequest> = requests
    .sortedByDescending { it.createdAt }
    .map { LocalFriendRequest(nameOf(it.senderUuid) ?: it.senderUuid.toString(), it.createdAt) }

/**
 * The open friend requests of [player] as they are shown to them.
 */
suspend fun friendRequestListComponent(player: UUID, page: Int = 1): Component {
    val friendRequests = SurfFriendsApi.receivedFriendRequests(player)

    if (friendRequests.isEmpty()) {
        return buildText {
            appendInfoPrefix()
            info("Du hast keine offenen Freundschaftsanfragen.")
        }
    }

    // Names come from surf-core first: PlayerLookupService alone does not know every account.
    val requesterEntries = friendRequestRows(friendRequests, ::resolveUsername)

    return buildText {
        appendNewline()
        append(pagination.renderComponent(requesterEntries, page))
    }
}
