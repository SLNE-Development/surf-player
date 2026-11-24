package dev.slne.surf.player.core.client.playtime.streak

/**
 * The login streak of a player as cached on this server.
 */
data class LoginStreak(
    val currentLoginStreak: Int,
    val longestLoginStreak: Int
)
