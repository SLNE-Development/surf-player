package dev.slne.surf.player.core.client.chat.permission

/**
 * Every permission node of the chat feature, the same on every platform.
 *
 * See docs/command-conventions.md for the scheme.
 */
object ChatPermissions {
    private const val COMMAND = "surf.player.command"
    private const val TEAM = "surf.player.team"
    private const val ADMIN = "surf.player.admin.chat"
    private const val PREFIX = "surf.player.chat"

    // Player commands
    const val COMMAND_MSG = "$COMMAND.msg"
    const val COMMAND_REPLY = "$COMMAND.reply"
    const val COMMAND_IGNORE = "$COMMAND.ignore"
    const val COMMAND_IGNORE_ADD = "$COMMAND_IGNORE.add"
    const val COMMAND_IGNORE_REMOVE = "$COMMAND_IGNORE.remove"
    const val COMMAND_IGNORE_LIST = "$COMMAND_IGNORE.list"

    // Team commands
    const val TEAM_TEAMCHAT = "$TEAM.teamchat"
    const val TEAM_SPY = "$TEAM.spy"
    const val TEAM_SLOWCHAT = "$TEAM.slowchat"

    // Admin actions (/surfplayer chat ...)
    const val ADMIN_RELOAD = "$ADMIN.reload"
    const val ADMIN_LOOKUP = "$ADMIN.lookup"
    const val ADMIN_STATUS = "$ADMIN.status"
    const val ADMIN_TOGGLE = "$ADMIN.toggle"
    const val ADMIN_SERVERS = "$ADMIN.servers"

    // Bypasses
    const val BYPASS_FILTER = "$PREFIX.bypass.filter"
    const val BYPASS_DISABLING = "$PREFIX.bypass.disabling"
    const val BYPASS_FUNCTIONALITY = "$PREFIX.bypass.functionality"
    const val BYPASS_SPY = "$PREFIX.bypass.spy"
    const val SLOW_CHAT_BYPASS = "$PREFIX.bypass.slowchat"

    /**
     * Team members: cannot be ignored. `/ignore add` refuses them, and their messages, private
     * messages and friend requests reach players who ignored them before.
     */
    const val IGNORE_EXEMPT = "$PREFIX.ignore.exempt"

    // Extra visibility
    /**
     * Sees every join and quit message: also with the setting `connection-messages` off, with
     * connection messages disabled in the config and, if allowed to see vanished players, those of
     * vanished players.
     */
    const val CONNECTION_MESSAGE_ALWAYS_SHOW = "$PREFIX.connection.always-show"
    const val SLOW_CHAT_NOTIFY = "$PREFIX.notify.slowchat"
    const val TEAM_NOTIFY_FUNCTIONALITY = "$PREFIX.notify.functionality"
    const val TEAM_NOTIFY_DELETION = "$PREFIX.notify.deletion"
    const val TEAM_NOTIFY_MODERATION = "$PREFIX.notify.moderation"

    // Buttons on chat messages
    const val DELETE_MESSAGE = "$PREFIX.delete"
    const val TELEPORT = "$PREFIX.teleport"
}
