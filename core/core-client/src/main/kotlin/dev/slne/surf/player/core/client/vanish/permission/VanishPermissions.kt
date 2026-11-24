package dev.slne.surf.player.core.client.vanish.permission

/**
 * Every permission node of the vanish feature.
 *
 * See docs/command-conventions.md for the scheme.
 */
object VanishPermissions {
    private const val PREFIX = "surf.player.vanish"

    // Team commands (/vanish, /vanish spectate)
    const val TEAM_VANISH = "surf.player.team.vanish"

    // Admin actions (/surfplayer vanish ...)
    const val ADMIN_RELOAD = "surf.player.admin.vanish.reload"

    /** Sees every vanished player regardless of their priority. */
    const val VANISH_BYPASS = "$PREFIX.bypass"

    /** Keeps the fly state when vanishing and reappearing. */
    const val VANISH_SAVE_FLY_STATE = "$PREFIX.save-fly-state"

    /** Prefix of `surf.player.vanish.priority.<n>`: players see vanished players up to their own priority. */
    const val VANISH_PRIORITY = "$PREFIX.priority"
}
