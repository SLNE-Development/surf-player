package dev.slne.surf.player.core.client.settings.menu

import net.kyori.adventure.key.Key

/**
 * A page of the settings menu.
 *
 * @property icon the key of the item type shown for this category
 */
enum class SettingCategory(
    val displayName: String,
    val description: String,
    val icon: Key,
) {
    CHAT("Chat", "Nachrichten und Benachrichtigungen im Chat", Key.key("oak_sign")),
    CLAN("Clan", "Clan-Chat und Clan-Einladungen", Key.key("white_banner")),
    FRIENDS("Freunde", "Benachrichtigungen und Sounds deiner Freunde", Key.key("poppy")),
    LOBBY("Lobby", "Einstellungen für die Lobby", Key.key("compass")),
    GENERAL("Allgemeines", "Anzeige- und Verbindungseinstellungen", Key.key("comparator")),
}
