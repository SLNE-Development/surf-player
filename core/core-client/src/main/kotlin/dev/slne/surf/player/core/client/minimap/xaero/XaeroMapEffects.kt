package dev.slne.surf.player.core.client.minimap.xaero

import dev.slne.surf.player.core.client.minimap.platform.MinimapPlayer
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Turns Xaero's maps off while `disable-maps` is on, through the status effects that make the mods hide them.
 *
 * Paper and Minestom do not know these effects, so the companion works out the network IDs the client gave them:
 * Minecraft's own effects come first, then the minimap's eight and the world map's four, in the order each mod
 * registers them. That holds as long as the client loads the minimap before the world map and no other mod adds
 * effects before them.
 */
object XaeroMapEffects {
    private const val MINIMAP_EFFECTS = 8
    private const val NO_MINIMAP = 0
    private const val NO_WORLD_MAP = 0

    private val given = ConcurrentHashMap<UUID, Set<Int>>()

    /**
     * Gives [player] the effects that hide each of Xaero's maps their client has while [disableMaps] is on,
     * and takes back the ones given before once it is off.
     */
    fun sync(player: MinimapPlayer, disableMaps: Boolean) {
        val effects = if (disableMaps) mapsOffEffects(player) else emptySet()
        val previous = if (effects.isEmpty()) given.remove(player.uuid) else given.put(player.uuid, effects)

        for (effect in previous.orEmpty() - effects) {
            player.removeClientEffect(effect)
        }
        // Sent again every time: the client drops them on every respawn and world change.
        for (effect in effects) {
            player.addClientEffect(effect)
        }
    }

    private fun mapsOffEffects(player: MinimapPlayer): Set<Int> {
        val vanillaEffects = player.vanillaEffectCount ?: return emptySet()
        val hasMinimap = player.hasChannel(XaeroChannels.MINIMAP)

        return buildSet {
            if (hasMinimap) add(vanillaEffects + NO_MINIMAP)
            if (player.hasChannel(XaeroChannels.WORLD_MAP)) {
                add(vanillaEffects + (if (hasMinimap) MINIMAP_EFFECTS else 0) + NO_WORLD_MAP)
            }
        }
    }

    fun forget(playerUuid: UUID) {
        given.remove(playerUuid)
    }
}
