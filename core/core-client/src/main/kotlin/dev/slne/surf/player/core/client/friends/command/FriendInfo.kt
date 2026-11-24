package dev.slne.surf.player.core.client.friends.command

import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.player.api.SurfCommunityPlayer
import dev.slne.surf.player.api.friends.utils.displayName
import dev.slne.surf.player.api.friends.utils.toSurfPlayer
import dev.slne.surf.player.core.client.friends.util.formatComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import java.util.*

/**
 * The details of the friendship between [player] and [target] as they are shown to [player].
 */
suspend fun friendInfoComponent(player: UUID, target: UUID): Component {
    val friendShip = SurfCommunityPlayer(player).findFriendship(target)
        ?: error("No friendship found between $player and $target")
    val targetSurfPlayer = target.toSurfPlayer()

    return buildText {
        appendInfoPrefix()
        primary("Freundschaftsinformationen".toSmallCaps(), TextDecoration.BOLD)
        appendNewline()

        appendInfoPrefix()
        variableKey("Freund: ".toSmallCaps())
        append(targetSurfPlayer.displayName())
        appendNewline()

        appendInfoPrefix()
        variableKey("Befreundet seit: ".toSmallCaps())
        append(friendShip.createdAt.formatComponent())
    }
}
