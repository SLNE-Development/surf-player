package dev.slne.surf.player.core.client.chat.message

/**
 * How the chat feature handles the vanilla join or quit message of a player on Paper.
 *
 * Matches the original surf-chat behaviour: a message another plugin removed is left alone, and
 * the announcement (the surf `PlayerJoinMessageEvent` / `PlayerQuitMessageEvent`, our message and,
 * on join, the chat MOTD) only happens when connection messages are enabled and the player is not
 * vanished. Who sees our message is decided by [ConnectionMessageRecipients].
 */
enum class ConnectionMessageMode {
    /** Another plugin removed the vanilla message: do nothing (no event, no message, no MOTD). */
    LEAVE_UNTOUCHED,

    /**
     * Remove the vanilla message and send nothing (no event, no message, no MOTD). Used for
     * vanished players and with connection messages disabled in the config.
     */
    SUPPRESS,

    /**
     * Remove the vanilla message, fire the surf message event and, unless it is cancelled, send
     * the (possibly changed) message to the recipients and, on join, the MOTD to the player.
     */
    ANNOUNCE,
}

object ConnectionMessageHandling {
    /**
     * @param vanillaMessagePresent whether the vanilla join/quit message is still set when the chat
     * listener runs
     * @param configEnabled `connectionMessageConfig.enabled`
     * @param playerVanished whether the joining or leaving player is vanished. The vanish feature
     * removes the vanilla message of vanished players itself, so this is checked first.
     */
    fun mode(
        vanillaMessagePresent: Boolean,
        configEnabled: Boolean,
        playerVanished: Boolean,
    ): ConnectionMessageMode = when {
        playerVanished -> ConnectionMessageMode.SUPPRESS
        !vanillaMessagePresent -> ConnectionMessageMode.LEAVE_UNTOUCHED
        !configEnabled -> ConnectionMessageMode.SUPPRESS
        else -> ConnectionMessageMode.ANNOUNCE
    }
}
