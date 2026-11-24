package dev.slne.surf.player.core.common.playtime.rpc

import dev.slne.surf.player.api.playtime.session.PlaytimeStreak
import kotlinx.serialization.Serializable

/** The outcome of [PlaytimeRpcService.updateLoginStreak]; [streak] is the stored streak afterwards. */
@Serializable
sealed interface LoginStreakUpdate {
    val streak: PlaytimeStreak

    /** The player had no stored streak; it was calculated from their sessions. */
    @Serializable
    data class Created(override val streak: PlaytimeStreak) : LoginStreakUpdate

    /** The stored streak was continued, kept (second login of the day) or reset to one day. */
    @Serializable
    data class Updated(override val streak: PlaytimeStreak) : LoginStreakUpdate
}
