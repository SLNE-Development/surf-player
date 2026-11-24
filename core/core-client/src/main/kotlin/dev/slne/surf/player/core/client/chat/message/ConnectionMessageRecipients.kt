package dev.slne.surf.player.core.client.chat.message

import dev.slne.surf.player.core.client.chat.permission.ChatPermissions

/**
 * Who sees the announced join and quit message (`[+] Name` / `[-] Name`) of a player.
 *
 * As in the original surf-chat, [ChatPermissions.CONNECTION_MESSAGE_ALWAYS_SHOW] belongs to the
 * joining or leaving player: their message reaches everyone. Without it, only viewers with the
 * setting `chat:connection-messages` enabled see it. Whether a message is announced at all
 * (config, vanish) is decided before, see [ConnectionMessageHandling].
 */
object ConnectionMessageRecipients {
    /**
     * @param viewerSettingEnabled the viewer's setting `chat:connection-messages`, asked only if
     * needed
     */
    inline fun receives(
        playerAlwaysShow: Boolean,
        viewerSettingEnabled: () -> Boolean,
    ): Boolean = playerAlwaysShow || viewerSettingEnabled()
}
