package dev.slne.surf.player.core.client.nametag.config

/**
 * Which players push each other while a nametag team is active.
 */
enum class NametagCollisionRule {
    ALWAYS,
    NEVER,
    PUSH_OTHER_TEAMS,
    PUSH_OWN_TEAM
}
