package dev.slne.surf.player.core.client.minimap.permission

/**
 * Every permission node of the minimap feature, the same on every platform.
 */
object MinimapPermissions {
    private const val ADMIN = "surf.player.admin.minimap"

    // Admin actions (/surfplayer minimap ...)
    const val ADMIN_RELOAD = "$ADMIN.reload"
    const val ADMIN_INFO = "$ADMIN.info"
}
