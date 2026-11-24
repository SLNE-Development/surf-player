package dev.slne.surf.social.core.client.minimap.voxelmap

import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Builds the settings JSON VoxelMap applies.
 *
 * Every key is always sent: Forge and NeoForge clients keep the last backend's settings until they log out,
 * so a key left out would keep another server's restriction. VoxelMap casts each value and disconnects on a
 * wrong type, so the switches are always JSON booleans.
 */
object VoxelMapSettings {

    /**
     * The settings [profile] enforces, with every switch off while [disableMaps] is on. `serverIdentity` is always
     * `null`, so the client keeps its map data where it is; a `null` teleportCommand means the client's own command.
     */
    fun json(profile: VoxelMapProfile, disableMaps: Boolean): String = buildJsonObject {
        put("serverIdentity", JsonNull)
        for (key in VoxelMapProfile.BOOLEANS) {
            put(key, !disableMaps && (profile.values[key] as? Boolean ?: true))
        }
        put(VoxelMapProfile.TELEPORT_COMMAND, profile.values[VoxelMapProfile.TELEPORT_COMMAND] as? String)
    }.toString()
}
