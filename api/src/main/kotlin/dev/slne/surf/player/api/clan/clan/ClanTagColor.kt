package dev.slne.surf.player.api.clan.clan

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.format.ShadowColor
import net.kyori.adventure.text.format.TextColor

@Serializable
data class ClanTagColor(
    val foregroundColor: @Contextual TextColor = Clan.DEFAULT_CLAN_TAG_FOREGROUND_COLOR,
    val backgroundColor: @Contextual TextColor = Clan.DEFAULT_CLAN_TAG_BACKGROUND_COLOR,
    val shadowColor: @Contextual ShadowColor = Clan.DEFAULT_CLAN_TAG_SHADOW_COLOR
) {

    companion object {
        inline fun update(block: ClanTagColorUpdateBuilder.() -> Unit): ClanTagColorUpdate {
            return ClanTagColorUpdateBuilder().apply(block).build()
        }

        fun clanTagColorOrNull(
            foreground: TextColor?,
            background: TextColor?,
            shadow: ShadowColor?
        ): ClanTagColor? {
            if (foreground == null || background == null || shadow == null) return null

            return ClanTagColor(
                foregroundColor = foreground,
                backgroundColor = background,
                shadowColor = shadow
            )
        }
    }

    fun applyUpdate(update: ClanTagColorUpdate): ClanTagColor = copy(
        foregroundColor = update.foreground.resolve(
            foregroundColor,
            Clan.DEFAULT_CLAN_TAG_FOREGROUND_COLOR
        ),
        backgroundColor = update.background.resolve(
            backgroundColor,
            Clan.DEFAULT_CLAN_TAG_BACKGROUND_COLOR
        ),
        shadowColor = update.shadow.resolve(
            shadowColor,
            Clan.DEFAULT_CLAN_TAG_SHADOW_COLOR
        ),
    )
}

/** Applies [update] to this color, starting from the default colors when no color is set yet. */
fun ClanTagColor?.update(update: ClanTagColorUpdate): ClanTagColor =
    (this ?: ClanTagColor()).applyUpdate(update)
