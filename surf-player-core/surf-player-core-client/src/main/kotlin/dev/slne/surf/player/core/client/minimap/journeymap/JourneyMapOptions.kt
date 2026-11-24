package dev.slne.surf.player.core.client.minimap.journeymap

/**
 * The value types of JourneyMap's server options.
 */
enum class JourneyMapOptionType {
    BOOLEAN,

    /**
     * JourneyMap's `ServerOption`. Only `ALL` and `NONE`: `OPS` would need settings per player.
     */
    SERVER_OPTION,

    /**
     * JourneyMap's `TeleportOption`. The client resolves `OPS` itself and the companion is never a
     * server admin, so `OPS` means nobody.
     */
    TELEPORT_OPTION,
    INT,
}

/**
 * One of JourneyMap's server options, under the field name JourneyMap's JSON uses.
 */
class JourneyMapOption(val id: String, val type: JourneyMapOptionType, val range: IntRange? = null)

/**
 * The server options JourneyMap 6.0.10's client reads, with the ranges its config allows.
 */
object JourneyMapOptions {
    /**
     * Options only the companion sets: `journeymapEnabled` follows `disable-maps`, `worldPlayerRadar` the tracker.
     */
    val COMPANION_OWNED = setOf("journeymapEnabled", "worldPlayerRadar")

    val options = listOf(
        bool("minimapEnabled"),
        bool("hideCoordinates"),
        bool("playerRadarEnabled"),
        bool("playerRadarNamesEnabled"),
        bool("villagerRadarEnabled"),
        bool("animalRadarEnabled"),
        bool("mobRadarEnabled"),
        bool("allowWaypoints"),
        bool("allowDeathPoints"),
        bool("showInGameBeacons"),
        bool("globalWaypointsOnly"),
        serverOption("surfaceMapping"),
        serverOption("topoMapping"),
        serverOption("biomeMapping"),
        serverOption("caveMapping"),
        serverOption("radarEnabled"),
        JourneyMapOption("teleport", JourneyMapOptionType.TELEPORT_OPTION),
        int("surfaceRenderRange", 0..32),
        int("caveRenderRange", 0..32),
        int("radarLateralDistance", 16..512),
        int("radarVerticalDistance", 8..320),
        int("maxAnimalsData", 1..128),
        int("maxAmbientCreaturesData", 1..128),
        int("maxMobsData", 1..128),
        int("maxPlayersData", 1..128),
        int("maxVillagersData", 1..128),
    )

    private val byId = options.associateBy { it.id }

    operator fun get(id: String) = byId[id]

    private fun bool(id: String) = JourneyMapOption(id, JourneyMapOptionType.BOOLEAN)

    private fun serverOption(id: String) = JourneyMapOption(id, JourneyMapOptionType.SERVER_OPTION)

    private fun int(id: String, range: IntRange) = JourneyMapOption(id, JourneyMapOptionType.INT, range)
}
