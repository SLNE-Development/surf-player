package dev.slne.surf.social.core.client.minimap.journeymap

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Builds the permissions JSON JourneyMap applies: its field names as keys, every value as a string.
 * Fields left out keep the client's defaults.
 */
object JourneyMapPermissions {

    /**
     * The fields the companion owns come first: JourneyMap turns itself off while [disableMaps] is on, and shows
     * only the players the server sends while [playerRadar] is on. The enforced options in [profile] follow.
     */
    fun json(profile: JourneyMapProfile, disableMaps: Boolean, playerRadar: Boolean) = buildJsonObject {
        put("journeymapEnabled", (!disableMaps).toString())
        put("worldPlayerRadar", if (playerRadar) "ALL" else "NONE")
        for ((field, value) in profile.values) {
            put(field, value)
        }
    }.toString()
}
