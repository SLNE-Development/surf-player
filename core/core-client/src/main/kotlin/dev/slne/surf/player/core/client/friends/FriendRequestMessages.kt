package dev.slne.surf.player.core.client.friends

import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.player.api.friends.result.FriendRequestCreateResult
import dev.slne.surf.player.api.friends.utils.displayName
import net.kyori.adventure.text.Component

/**
 * The message the sender sees for [FriendRequestCreateResult.RequestsDisabled]: [target] turned
 * off the setting `friend:requests`.
 */
fun friendRequestsDisabledMessage(target: SurfPlayer?): Component = buildText {
    appendErrorPrefix()
    append(target.displayName())
    error(" nimmt keine Freundschaftsanfragen an.")
}
