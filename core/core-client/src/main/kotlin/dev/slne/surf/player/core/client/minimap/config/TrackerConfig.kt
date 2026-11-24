package dev.slne.surf.player.core.client.minimap.config

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment

@ConfigSerializable
data class TrackerConfig(
    @param:Comment("Shows every player on this server on everyone's map, like Xaero's everyone_tracks_everyone.")
    val everyoneTracksEveryone: Boolean = false,

    @param:Comment("Shows players their friends. Paper only.")
    val friends: Boolean = false,

    @param:Comment("Shows players the members of their clan. Paper only.")
    val clanMembers: Boolean = false,
) {
    /**
     * Whether any switch shows players beyond view distance.
     */
    val isEnabled: Boolean get() = everyoneTracksEveryone || friends || clanMembers
}
