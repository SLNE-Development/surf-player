package dev.slne.surf.social.core.client.tab.entry

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import java.lang.ref.WeakReference

/**
 * The tab list entry last shown for a player, kept to suppress identical updates.
 */
internal class ShownEntry(audience: Audience, val name: Component, val order: Int) {
    private val audience = WeakReference(audience)

    fun sameAs(other: ShownEntry): Boolean {
        return audience.get() === other.audience.get() && name == other.name && order == other.order
    }
}
