package dev.slne.surf.player.core.common

/**
 * The key of a Redis structure owned by surf-player: `surf-player:<feature>:<name>`.
 *
 * Every raw Redisson key, cache and sync structure of every feature is named with this function,
 * so all surf-player data on Redis lives below one prefix. See `docs/protocol.md`.
 */
fun playerRedisKey(feature: String, name: String): String = "$PLAYER_CONNECTION_NAME:$feature:$name"
