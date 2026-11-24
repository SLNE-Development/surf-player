package dev.slne.surf.player.microservice.friends.config

import dev.slne.surf.api.core.environment.EnvironmentVariables
import dev.slne.surf.player.microservice.friends.cooldown.FriendRequestCooldownSettings
import kotlin.time.Duration

/**
 * Friends configuration, read from environment variables like the rest of the microservice
 * configuration. Durations use Kotlin's duration format (`30m`, `24h`, `7d`) or ISO-8601 (`PT24H`).
 *
 * | Setting                       | Environment variable                                  | Default |
 * |-------------------------------|-------------------------------------------------------|---------|
 * | cooldown after a decline      | `SURF_PLAYER_FRIEND_REQUEST_DECLINE_COOLDOWN`         | `24h`   |
 * | cooldown after a withdrawal   | `SURF_PLAYER_FRIEND_REQUEST_WITHDRAW_COOLDOWN`        | `30m`   |
 * | escalation window             | `SURF_PLAYER_FRIEND_REQUEST_COOLDOWN_ESCALATION_WINDOW` | `30d` |
 * | maximum cooldown              | `SURF_PLAYER_FRIEND_REQUEST_MAX_COOLDOWN`             | `7d`    |
 */
object FriendsConfig {
    @Volatile
    var cooldownSettings: FriendRequestCooldownSettings = FriendRequestCooldownSettings.DEFAULT
        private set

    fun load() {
        val defaults = FriendRequestCooldownSettings.DEFAULT

        cooldownSettings = FriendRequestCooldownSettings(
            declineCooldown = positiveDuration(
                "SURF_PLAYER_FRIEND_REQUEST_DECLINE_COOLDOWN",
                defaults.declineCooldown
            ),
            withdrawCooldown = positiveDuration(
                "SURF_PLAYER_FRIEND_REQUEST_WITHDRAW_COOLDOWN",
                defaults.withdrawCooldown
            ),
            escalationWindow = positiveDuration(
                "SURF_PLAYER_FRIEND_REQUEST_COOLDOWN_ESCALATION_WINDOW",
                defaults.escalationWindow
            ),
            maxCooldown = positiveDuration(
                "SURF_PLAYER_FRIEND_REQUEST_MAX_COOLDOWN",
                defaults.maxCooldown
            )
        )
    }

    private fun positiveDuration(name: String, default: Duration): Duration {
        val duration = EnvironmentVariables.system.duration(name, default)
        require(duration.isPositive() && duration.isFinite()) { "$name must be a positive duration, got $duration" }
        return duration
    }
}
