package dev.slne.surf.player.core.client.vanish

import dev.slne.surf.player.core.client.vanish.permission.VanishPermissions

/**
 * The one rule deciding who sees a vanished player, on every platform: in game on Paper (and the
 * notices about them, e.g. "… hat den Server unsichtbar betreten"), in the tab list, on the maps,
 * in friend notices and lists, and in command suggestions and name lookups ([PlayerVisibility]).
 */
object VanishSight {
    private const val PRIORITY_PREFIX = "${VanishPermissions.VANISH_PRIORITY}."

    /**
     * The highest `surf.player.vanish.priority.<n>` among [grantedPermissions] (the permission
     * nodes a player holds with value `true`), `null` without any.
     */
    fun explicitPriority(grantedPermissions: Sequence<String>): Int? =
        grantedPermissions
            .filter { it.startsWith(PRIORITY_PREFIX) }
            .mapNotNull { it.removePrefix(PRIORITY_PREFIX).toIntOrNull() }
            .maxOrNull()

    /**
     * The vanish priority of a viewer: the highest `surf.player.vanish.priority.<n>` they have;
     * staff who may vanish themselves ([VanishPermissions.TEAM_VANISH]) without such a node have
     * priority 0; everyone else has none (`null`) and sees no vanished player.
     */
    fun effectivePriority(explicitPriority: Int?, mayVanish: Boolean): Int? =
        explicitPriority ?: if (mayVanish) 0 else null

    /**
     * The vanish priority of a viewer from the permission nodes they hold ([grantedPermissions])
     * and their permission check ([hasPermission]), see [effectivePriority].
     */
    fun viewerPriority(grantedPermissions: Sequence<String>, hasPermission: (String) -> Boolean): Int? =
        effectivePriority(explicitPriority(grantedPermissions), hasPermission(VanishPermissions.TEAM_VANISH))

    /**
     * The priority a player is hidden with while vanished: their highest
     * `surf.player.vanish.priority.<n>`, 0 without any. Stored network-wide in [VanishedPlayers].
     */
    fun targetPriority(grantedPermissions: Sequence<String>): Int = explicitPriority(grantedPermissions) ?: 0

    /**
     * Whether a viewer sees a vanished target: the target themself, viewers with
     * [VanishPermissions.VANISH_BYPASS], and viewers with a priority (see [effectivePriority]) at
     * least as high as the target's ([targetPriority], `null` counts as 0). A player without any
     * vanish permission never does.
     */
    fun canSee(viewerIsTarget: Boolean, viewerHasBypass: Boolean, viewerPriority: Int?, targetPriority: Int?): Boolean =
        viewerIsTarget || viewerHasBypass || (viewerPriority != null && viewerPriority >= (targetPriority ?: 0))
}
