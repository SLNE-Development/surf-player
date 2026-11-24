package dev.slne.surf.player.core.client.contentcreator.permission

/**
 * Every permission node of the content creator feature, the same on every platform.
 */
object ContentCreatorPermissions {
    private const val ADMIN = "surf.player.admin.contentcreator"

    // Admin actions (/surfplayer contentcreator ...)
    const val ADMIN_RELOAD = "$ADMIN.reload"
    const val ADMIN_DEBUG = "$ADMIN.debug"
}
