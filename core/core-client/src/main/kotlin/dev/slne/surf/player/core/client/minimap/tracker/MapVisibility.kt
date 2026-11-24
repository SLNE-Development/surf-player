package dev.slne.surf.player.core.client.minimap.tracker

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Whether a viewer sees another player on the maps: exactly when they see that player in the world. The vanish
 * feature hides vanished players from the viewers whose level may not see them, so vanished players
 * stay visible to the viewers who may.
 */
object MapVisibility {

    /**
     * How long a vanished player stays off every map after joining. Vanish hides a joining vanished player from
     * the other players only a little after the join, and the maps must not show them in between.
     */
    val VANISH_GRACE = 1.seconds

    /**
     * [canSee] tells whether the viewer sees the target in the world, [onlineFor] how long the target has been
     * online. [isVanished] is only asked for targets that joined within [VANISH_GRACE].
     */
    fun isShown(canSee: Boolean, onlineFor: Duration, isVanished: () -> Boolean): Boolean =
        canSee && (onlineFor >= VANISH_GRACE || !isVanished())
}
