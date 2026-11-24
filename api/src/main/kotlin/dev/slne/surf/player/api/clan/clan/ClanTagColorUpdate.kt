package dev.slne.surf.player.api.clan.clan

import net.kyori.adventure.text.format.ShadowColor
import net.kyori.adventure.text.format.TextColor

/**
 * A change to some of the colors of a [ClanTagColor]; build one with [ClanTagColor.update].
 */
@ConsistentCopyVisibility
data class ClanTagColorUpdate internal constructor(
    internal val foreground: ClanTagColorField<TextColor>,
    internal val background: ClanTagColorField<TextColor>,
    internal val shadow: ClanTagColorField<ShadowColor>
)

class ClanTagColorUpdateBuilder @PublishedApi internal constructor() {
    private var foreground: ClanTagColorField<TextColor> = ClanTagColorField.Unset
    private var background: ClanTagColorField<TextColor> = ClanTagColorField.Unset
    private var shadow: ClanTagColorField<ShadowColor> = ClanTagColorField.Unset

    fun foreground(color: TextColor) {
        foreground = ClanTagColorField.Set(color)
    }

    fun background(color: TextColor) {
        background = ClanTagColorField.Set(color)
    }

    fun shadow(color: ShadowColor) {
        shadow = ClanTagColorField.Set(color)
    }

    fun resetForeground() {
        foreground = ClanTagColorField.Reset
    }

    fun resetBackground() {
        background = ClanTagColorField.Reset
    }

    fun resetShadow() {
        shadow = ClanTagColorField.Reset
    }

    @PublishedApi
    internal fun build() = ClanTagColorUpdate(foreground, background, shadow)
}

/** What a [ClanTagColorUpdate] does with one color. */
internal sealed interface ClanTagColorField<out T> {
    /** Keep the current color. */
    data object Unset : ClanTagColorField<Nothing>

    /** Go back to the default color. */
    data object Reset : ClanTagColorField<Nothing>

    /** Use [value]. */
    data class Set<T>(val value: T) : ClanTagColorField<T>

    fun resolve(current: @UnsafeVariance T, default: @UnsafeVariance T): T = when (this) {
        Unset -> current
        Reset -> default
        is Set -> value
    }
}
