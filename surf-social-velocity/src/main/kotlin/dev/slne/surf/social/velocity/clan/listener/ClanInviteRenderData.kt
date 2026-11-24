package dev.slne.surf.social.velocity.clan.listener

import dev.slne.surf.api.core.messages.adventure.buildText
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.event.ClickEvent

/**
 * One pending clan invitation as listed to the invited player, with buttons to accept or deny it.
 */
internal data class ClanInviteRenderData(
    private val clanInformationHover: Component,
    private val clanName: String
) : ComponentLike {
    override fun asComponent(): Component = buildText {
        append(clanInformationHover)
        appendSpace()
        append {
            success("[Annehmen]")
            hoverEvent(buildText {
                info("Klicke hier, um die Einladung anzunehmen.")
            })
            clickEvent(ClickEvent.runCommand("/clan accept $clanName"))
        }
        appendSpace()
        append {
            error("[Ablehnen]")
            hoverEvent(buildText {
                info("Klicke hier, um die Einladung abzulehnen.")
            })
            clickEvent(ClickEvent.runCommand("/clan deny $clanName"))
        }
    }
}
