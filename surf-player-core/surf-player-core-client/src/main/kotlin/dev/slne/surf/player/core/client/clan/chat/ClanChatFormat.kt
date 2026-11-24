package dev.slne.surf.player.core.client.clan.chat

import dev.slne.surf.api.core.messages.adventure.buildText
import net.kyori.adventure.text.Component

/**
 * Renders [message] the way clan chat shows it, attributed to [senderName].
 */
fun formatClanChatMessage(message: String, senderName: Component) = buildText {
    darkSpacer(">>")
    appendSpace()
    primary("Clan")
    appendSpace()
    darkSpacer("|")
    appendSpace()
    append(senderName)
    spacer(":")
    appendSpace()
    white(message)
    clickSuggestsCommand("/clan chat ")
}
