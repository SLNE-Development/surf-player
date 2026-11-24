package dev.slne.surf.player.api.clan.clan.listener

import dev.slne.surf.player.api.clan.clan.Clan

/**
 * Listener for clan creation events.
 *
 * This functional interface is invoked whenever a new clan is created.
 * As a SAM (Single Abstract Method) interface, it can be implemented using lambda expressions.
 *
 * Example usage:
 * ```kotlin
 * val listener = ClanCreatedListener { clan ->
 *     println("New clan created: ${clan.name}")
 * }
 * SurfClanApi.registerListener(listener)
 * ```
 *
 * @see ClanListener
 * @see dev.slne.surf.player.api.clan.SurfClanApi.registerListener
 */
fun interface ClanCreatedListener : ClanListener {
    /**
     * Called when a new clan has been created.
     *
     * This method is invoked after the clan has been successfully persisted.
     *
     * @param clan the newly created clan
     */
    fun onClanCreated(clan: Clan)
}