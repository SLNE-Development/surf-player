package dev.slne.surf.social.core.client.tab.command

import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.social.core.client.tab.service.TablistService
import net.kyori.adventure.audience.Audience

/**
 * Reloads the tablist configuration and applies it to everyone currently online.
 */
fun reloadTablist() = TablistService.reload()

fun Audience.sendTablistReloaded() = sendText {
    appendSuccessPrefix()
    success("Die Tablist wurde neu geladen.")
}
