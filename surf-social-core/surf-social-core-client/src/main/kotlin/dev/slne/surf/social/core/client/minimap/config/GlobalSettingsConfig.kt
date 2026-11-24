package dev.slne.surf.minimapcompanion.core.config

import dev.slne.surf.minimapcompanion.core.global.GlobalFeature
import org.spongepowered.configurate.BasicConfigurationNode
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment

private fun perMod(): ConfigurationNode = BasicConfigurationNode.root().raw("per-mod")

/**
 * What `global-settings` in `config.yml` holds: per-mod, true or false for each [GlobalFeature]. Each entry stays
 * the node the file holds, so a value the companion does not understand is written back as the admin wrote it.
 */
@ConfigSerializable
data class GlobalSettingsConfig(
    @param:Comment("The minimap.")
    val minimap: ConfigurationNode = perMod(),

    @param:Comment("Cave mode. true also lifts Xaero's dimension lists.")
    val caveMode: ConfigurationNode = perMod(),

    @param:Comment("The entity radar.")
    val radar: ConfigurationNode = perMod(),

    @param:Comment("Waypoints. Xaero can only hide them; players can still create them.")
    val waypoints: ConfigurationNode = perMod(),

    @param:Comment("Death waypoints. false deletes the existing ones in JourneyMap.")
    val deathWaypoints: ConfigurationNode = perMod(),

    @param:Comment("Teleporting from the map, in Xaero's World Map and JourneyMap.")
    val teleport: ConfigurationNode = perMod(),

    @param:Comment("Players on the radar, in JourneyMap and VoxelMap.")
    val radarPlayers: ConfigurationNode = perMod(),

    @param:Comment("Mobs, animals and villagers on the radar, in JourneyMap and VoxelMap.")
    val radarMobs: ConfigurationNode = perMod(),

    @param:Comment("Coordinates on the maps, in Xaero's mods and JourneyMap.")
    val coordinates: ConfigurationNode = perMod(),
) {
    fun entries(): Map<GlobalFeature, ConfigurationNode> = mapOf(
        GlobalFeature.MINIMAP to minimap,
        GlobalFeature.CAVE_MODE to caveMode,
        GlobalFeature.RADAR to radar,
        GlobalFeature.WAYPOINTS to waypoints,
        GlobalFeature.DEATH_WAYPOINTS to deathWaypoints,
        GlobalFeature.TELEPORT to teleport,
        GlobalFeature.RADAR_PLAYERS to radarPlayers,
        GlobalFeature.RADAR_MOBS to radarMobs,
        GlobalFeature.COORDINATES to coordinates,
    )
}
