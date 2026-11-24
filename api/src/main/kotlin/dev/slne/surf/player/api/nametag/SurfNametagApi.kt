package dev.slne.surf.player.api.nametag

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.player.api.nametag.nametag.NametagVisibility
import java.util.*

private val instance = requiredService<SurfNametagApi>()

interface SurfNametagApi {

    /**
     * Stops updating the nametag of [playerUuid] until [allowNametagUpdates] is called.
     */
    fun preventNametagUpdates(playerUuid: UUID)

    /**
     * Updates the nametag of [playerUuid] again after [preventNametagUpdates].
     */
    fun allowNametagUpdates(playerUuid: UUID)

    /**
     * Shows the nametag of [playerUuid] with [visibility] to every viewer, instead of the configured
     * visibility.
     *
     * Viewers who already see the nametag are updated right away. The override lasts until
     * [resetNametagVisibility] or until the player leaves the server.
     */
    fun setNametagVisibility(playerUuid: UUID, visibility: NametagVisibility)

    /**
     * Returns the nametag of [playerUuid] to the configured visibility and updates every viewer.
     *
     * Does nothing when no override is set.
     */
    fun resetNametagVisibility(playerUuid: UUID)

    companion object : SurfNametagApi by instance {
        val INSTANCE: SurfNametagApi get() = instance
    }
}
