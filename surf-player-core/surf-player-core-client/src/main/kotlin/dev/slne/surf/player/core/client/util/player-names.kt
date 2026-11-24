package dev.slne.surf.player.core.client.util

import dev.slne.surf.api.core.service.PlayerLookupService
import java.util.*

/**
 * The name of the player identified by [uuid], falling back to the uuid itself where the name is
 * unknown.
 */
suspend fun usernameOrUuid(uuid: UUID): String =
    PlayerLookupService.getUsername(uuid) ?: uuid.toString()
