package dev.slne.surf.social.velocity.connection.redis

import kotlinx.serialization.Serializable

/**
 * The payload [DiscordRolesPublisher] sends to surf-discord; its JSON shape is shared with that
 * service.
 */
@Serializable
data class DiscordRoleRequest(
    val action: DiscordRoleAction,
    val userId: Long
)
