package dev.slne.surf.minimapcompanion.core.config

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment

@ConfigSerializable
data class TrackerConfig(
    @param:Comment("Shows every player on this server on everyone's map, like Xaero's everyone_tracks_everyone.")
    val everyoneTracksEveryone: Boolean = false,

    @param:Comment("Shows players their friends from surf-friends. Needs surf-friends-paper on this server.")
    val friends: Boolean = false,

    @param:Comment("Shows players the members of their clan from surf-clan. Needs surf-clan-paper on this server.")
    val clanMembers: Boolean = false,
) {
    /**
     * Whether any switch shows players beyond view distance.
     */
    val isEnabled: Boolean get() = everyoneTracksEveryone || friends || clanMembers
}
