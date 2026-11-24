package dev.slne.surf.player.core.client.clan.command

import dev.slne.surf.player.api.clan.SurfClanApi
import dev.slne.surf.player.api.clan.clan.Clan
import dev.slne.surf.player.api.clan.member.ClanMemberRole
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.player.core.common.clan.clan.ClanImpl
import dev.slne.surf.player.core.client.clan.Messages
import dev.slne.surf.player.core.client.clan.components.ClanComponents
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.TextDecoration
import java.util.*

const val CLAN_OWNER_CANNOT_LEAVE =
    "Du bist der Besitzer des Clans und kannst ihn nicht verlassen. Nutze /clan delete, um den Clan aufzulösen."

/**
 * Whether [player] holds the owner role in this clan; the owner cannot leave it, only delete it.
 */
fun Clan.isOwnedBy(player: UUID): Boolean = getMember(player)?.role == ClanMemberRole.OWNER

/**
 * Asks for confirmation before the clan is left, with [click] carrying out the leave.
 */
suspend fun leaveConfirmationMessage(clan: Clan, click: ClickEvent<*>) = buildText {
    appendWarningPrefix()
    warning("Möchtest du den Clan ")
    append(ClanComponents.renderClanInformationHover(clan as ClanImpl))
    warning(" wirklich verlassen? Klicke ")
    append {
        error("HIER", TextDecoration.BOLD)
        hoverEvent(leaveHoverText())
        clickEvent(click)
    }
    warning(" um den Clan zu verlassen.")
}

private fun leaveHoverText() = buildText {
    info("Klicke hier um den Clan zu verlassen.")
    appendNewline(3)
    error("Achtung: ", TextDecoration.BOLD)
    error("Du kannst den Vorgang nicht rückgängig machen.")
    appendNewline(2)
    error("Wenn du den Clan verlässt, verlierst du alle Rechte")
    appendNewline()
    error("und benötigst erneut eine Einladung, um wieder den Clan beitreten zu können.")
}

/**
 * Removes [clicker] from the clan they confirmed leaving, provided they are still in that clan.
 */
suspend fun handleLeaveClick(
    clicked: Audience,
    clicker: UUID,
    clickerName: String,
    originalClanUuid: UUID
) {
    val clan = SurfClanApi.findClanByPlayer(clicker) ?: return clicked.sendText {
        appendErrorPrefix()
        error(Messages.NO_LONGER_IN_THAT_CLAN)
    }

    if (clan.uuid != originalClanUuid) return clicked.sendText {
        appendErrorPrefix()
        error("Der Clan, den du verlassen wolltest, hat sich geändert. Bitte versuche es erneut.")
    }

    if (clan.isOwnedBy(clicker)) return clicked.sendText {
        appendErrorPrefix()
        error(CLAN_OWNER_CANNOT_LEAVE)
    }

    val removed = clan.removeMember(clicker)

    if (!removed) {
        return clicked.sendText {
            appendErrorPrefix()
            error(Messages.NO_LONGER_IN_THAT_CLAN)
        }
    } else {
        clicked.sendText {
            appendSuccessPrefix()
            success("Du hast den Clan verlassen.")
        }

        val memberLeftMessage = buildText {
            appendInfoPrefix()
            variableValue(clickerName)
            info(" hat den Clan verlassen.")
        }

        clan.broadcast(memberLeftMessage)
    }
}
