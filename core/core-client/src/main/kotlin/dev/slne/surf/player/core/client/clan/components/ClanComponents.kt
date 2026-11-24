package dev.slne.surf.player.core.client.clan.components

import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.Colors
import dev.slne.surf.api.core.messages.CommonComponents
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.builder.SurfComponentBuilder
import dev.slne.surf.player.api.clan.clan.Clan.Companion.DISCORD_LINK_REQUIRED_MEMBERS
import dev.slne.surf.player.api.clan.member.ClanMemberRole
import dev.slne.surf.player.core.client.util.usernameOrUuid
import dev.slne.surf.player.core.common.clan.clan.ClanImpl
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.TextDecoration
import java.time.format.DateTimeFormatter

/**
 * The components that describe a clan, shared by every platform.
 */
object ClanComponents {
    private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")

    suspend fun renderClanInformationHover(
        clan: ClanImpl,
        withInvite: Boolean = true,
    ) = buildText {
        val hoverComponent = buildText {
            append(renderClanInformation(clan))

            if (withInvite) {
                appendNewline()
                appendNewline()

                text("Klicke, um eine Einladung zum Clan-Discord", Colors.GRAY)
                appendNewline()
                text("zu erhalten.", Colors.GRAY)
            }
        }

        variableValue(clan.name)
        hoverEvent(hoverComponent)

        if (withInvite) {
            clickEvent(discordClickEvent(clan))
        }
    }

    suspend fun renderClanInformation(clan: ClanImpl) = buildText {
        val createdBy = usernameOrUuid(clan.createdByUuid)
        val activeMemberCount = clan.activeMemberCount
        info("Informationen".toSmallCaps(), TextDecoration.BOLD)

        appendNewline {
            appendLine("Name".toSmallCaps(), clan.name)
        }

        appendNewline {
            appendLine("Tag".toSmallCaps(), clan.tag)
        }

        appendNewline {
            appendLine(
                "Anführer".toSmallCaps(),
                clan.members.count { it.role == ClanMemberRole.LEADER || it.role == ClanMemberRole.OWNER }
            )
        }

        appendNewline {
            appendLine(
                "Offiziere".toSmallCaps(),
                clan.members.count { it.role == ClanMemberRole.OFFICER }
            )
        }

        appendNewline {
            appendLine(
                "Mitglieder".toSmallCaps(),
                "$activeMemberCount aktiv / ${clan.members.size} gesamt"
            )
        }

        appendNewline {
            appendLine("Erstellt von".toSmallCaps(), createdBy)
        }

        appendNewline {
            appendLine("Erstellt am".toSmallCaps(), DATE_TIME_FORMATTER.format(clan.createdAt))
        }

        if (activeMemberCount >= DISCORD_LINK_REQUIRED_MEMBERS) {
            appendNewline {
                appendLine(
                    "Discord",
                    clan.discordInvite ?: CommonComponents.DISCORD_LINK.color(Colors.WHITE)
                )
                clickEvent(discordClickEvent(clan))
            }
        }
    }

    /** Opens the clan's own Discord invite, or the network's Discord where it has none. */
    private fun discordClickEvent(clan: ClanImpl) =
        clan.discordInvite?.let { ClickEvent.openUrl(it) } ?: CommonComponents.DISCORD_LINK.clickEvent()

    private fun SurfComponentBuilder.appendLine(key: String, value: Any) = append {
        info("| ", TextDecoration.BOLD)
        text("$key: ", Colors.GRAY)
        if (value is ComponentLike) {
            append(value)
        } else {
            text(value.toString(), Colors.WHITE)
        }
    }
}
