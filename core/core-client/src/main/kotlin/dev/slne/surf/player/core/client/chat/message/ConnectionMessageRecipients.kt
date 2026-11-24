package dev.slne.surf.player.core.client.chat.message

import dev.slne.surf.player.core.client.chat.permission.ChatPermissions

/**
 * Who sees the join and quit message (`[+] Name` / `[-] Name`) of a player.
 */
object ConnectionMessageRecipients {
    /**
     * Whether a viewer sees the connection message of the joining or leaving player.
     *
     * - The message of a vanished player is only shown to others who hold
     *   [ChatPermissions.CONNECTION_MESSAGE_ALWAYS_SHOW] and may see vanished players.
     * - Holders of [ChatPermissions.CONNECTION_MESSAGE_ALWAYS_SHOW] see every other message, even
     *   with connection messages disabled in the config ([configEnabled]) or in their own setting.
     * - Everyone else sees it if connection messages are enabled in the config and in their setting.
     *
     * @param viewerSettingEnabled the viewer's setting `connection-messages`, asked only if needed
     * @param viewerCanSeeVanished whether the viewer may see the vanished player, asked only if
     * needed
     */
    inline fun receives(
        configEnabled: Boolean,
        playerVanished: Boolean,
        viewerIsPlayer: Boolean,
        viewerAlwaysShow: Boolean,
        viewerSettingEnabled: () -> Boolean,
        viewerCanSeeVanished: () -> Boolean,
    ): Boolean = when {
        playerVanished -> !viewerIsPlayer && viewerAlwaysShow && viewerCanSeeVanished()
        viewerAlwaysShow -> true
        else -> configEnabled && viewerSettingEnabled()
    }
}
