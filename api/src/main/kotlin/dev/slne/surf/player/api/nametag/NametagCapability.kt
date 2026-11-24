package dev.slne.surf.player.api.nametag

import dev.slne.surf.player.api.nametag.nametag.NametagVisibility
import java.util.*

/** The nametag of a single player. See [SurfNametagApi]. */
interface NametagCapability {
    val uuid: UUID

    /** Stops updating this player's nametag until [allowNametagUpdates] is called. */
    fun preventNametagUpdates() = SurfNametagApi.preventNametagUpdates(uuid)

    /** Updates this player's nametag again after [preventNametagUpdates]. */
    fun allowNametagUpdates() = SurfNametagApi.allowNametagUpdates(uuid)

    /** Shows this player's nametag with [visibility] to every viewer. See [SurfNametagApi.setNametagVisibility]. */
    fun setNametagVisibility(visibility: NametagVisibility) =
        SurfNametagApi.setNametagVisibility(uuid, visibility)

    /** Returns this player's nametag to the configured visibility. */
    fun resetNametagVisibility() = SurfNametagApi.resetNametagVisibility(uuid)
}
