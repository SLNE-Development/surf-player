package dev.slne.surf.player.core.client.clan.permission

/**
 * Every permission node of the clan feature, the same on every platform.
 *
 * See docs/command-conventions.md for the scheme.
 */
object ClanPermissions {
    private const val COMMAND = "surf.player.command.clan"
    private const val ADMIN = "surf.player.admin.clan"
    private const val PREFIX = "surf.player.clan"

    // Player commands (/clan ..., /cc)
    const val COMMAND_CLAN = COMMAND
    const val COMMAND_CREATE = "$COMMAND.create"
    const val COMMAND_DELETE = "$COMMAND.delete"
    const val COMMAND_LEAVE = "$COMMAND.leave"
    const val COMMAND_INFO = "$COMMAND.info"
    const val COMMAND_WHOIS = "$COMMAND.whois"
    const val COMMAND_LIST = "$COMMAND.list"
    const val COMMAND_MEMBERS = "$COMMAND.members"
    const val COMMAND_INVITE = "$COMMAND.invite"
    const val COMMAND_REVOKE = "$COMMAND.revoke"
    const val COMMAND_ACCEPT = "$COMMAND.accept"
    const val COMMAND_DENY = "$COMMAND.deny"
    const val COMMAND_PROMOTE = "$COMMAND.promote"
    const val COMMAND_DEMOTE = "$COMMAND.demote"
    const val COMMAND_KICK = "$COMMAND.kick"
    const val COMMAND_DISCORD = "$COMMAND.discord"
    const val COMMAND_COLOR = "$COMMAND.color"
    const val COMMAND_CHAT = "$COMMAND.chat"

    // Perks: which parts of the clan tag the clan owner may colour
    const val TAG_COLOR_FOREGROUND = "$PREFIX.color.foreground"
    const val TAG_COLOR_BACKGROUND = "$PREFIX.color.background"
    const val TAG_COLOR_SHADOW = "$PREFIX.color.shadow"

    // Admin actions (/surfplayer clan ...)
    const val ADMIN_RELOAD = "$ADMIN.reload"
    const val ADMIN_INVALIDATE_CACHES = "$ADMIN.invalidatecaches"
    const val ADMIN_DELETE = "$ADMIN.delete"
}
