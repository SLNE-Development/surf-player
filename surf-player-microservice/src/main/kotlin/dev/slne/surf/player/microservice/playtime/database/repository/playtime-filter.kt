package dev.slne.surf.player.microservice.playtime.database.repository

import dev.slne.surf.player.api.playtime.session.PlaytimeFilter

/**
 * Whether one of the restrictions of this filter is an empty set, so no session can match.
 */
internal fun PlaytimeFilter.matchesNothing() =
    servers?.isEmpty() == true || categories?.isEmpty() == true || players?.isEmpty() == true
