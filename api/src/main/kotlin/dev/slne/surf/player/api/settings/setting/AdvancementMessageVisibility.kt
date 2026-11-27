package dev.slne.surf.player.api.settings.setting

/**
 * Which advancement messages a player sees in chat.
 */
enum class AdvancementMessageVisibility {
    /** The advancements of every player. */
    ALL,

    /** Only the player's own advancements. */
    OWN,

    /** No advancement messages at all. */
    NONE;

    /**
     * Whether a viewer with this visibility sees an advancement message; [ownAdvancement] tells
     * whether the viewer completed the advancement themselves.
     */
    fun shows(ownAdvancement: Boolean): Boolean = when (this) {
        ALL -> true
        OWN -> ownAdvancement
        NONE -> false
    }
}
