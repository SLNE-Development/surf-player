package dev.slne.surf.minimapcompanion.core.tracker

/**
 * Where a tracked player is: a position in the world with the key [dimension], such as `minecraft:overworld`.
 */
data class TrackedPosition(val x: Double, val y: Double, val z: Double, val dimension: String)
