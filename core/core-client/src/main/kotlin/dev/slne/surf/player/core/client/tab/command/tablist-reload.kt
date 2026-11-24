package dev.slne.surf.player.core.client.tab.command

import dev.slne.surf.player.core.client.tab.service.TablistService

/**
 * Reloads the tablist configuration and applies it to everyone currently online.
 */
fun reloadTablist() = TablistService.reload()
