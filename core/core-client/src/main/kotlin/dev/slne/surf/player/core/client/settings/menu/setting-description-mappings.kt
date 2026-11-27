package dev.slne.surf.player.core.client.settings.menu

import dev.slne.surf.player.api.settings.setting.AdvancementMessageVisibility
import dev.slne.surf.player.api.settings.setting.SettingKeys
import net.kyori.adventure.key.Key

/**
 * The settings shown in the settings menu by setting name, in display order.
 */
val settingKeyMappings = mapOf(
    SettingKeys.CONNECTION_MESSAGES to SettingKeyDisplay(
        "Verbindungsnachrichten",
        "Zeigt Nachrichten an, wenn Spieler den Server betreten oder verlassen",
        Key.key("ender_pearl"),
        SettingCategory.CHAT,
    ),
    SettingKeys.CHAT_DEATH_MESSAGES to SettingKeyDisplay(
        "Todesnachrichten",
        "Zeigt Todesnachrichten im Chat an",
        Key.key("red_bed"),
        SettingCategory.CHAT,
    ),
    SettingKeys.CHAT_ADVANCEMENT_MESSAGES to SettingKeyDisplay(
        "Fortschrittsnachrichten",
        "Legt fest, welche Fortschrittsnachrichten du im Chat siehst",
        Key.key("experience_bottle"),
        SettingCategory.CHAT,
        options = listOf(
            SettingOption(AdvancementMessageVisibility.ALL.name, "Alle"),
            SettingOption(AdvancementMessageVisibility.OWN.name, "Nur eigene"),
            SettingOption(AdvancementMessageVisibility.NONE.name, "Aus"),
        ),
    ),
    SettingKeys.DIRECT_MESSAGES to SettingKeyDisplay(
        "Private Nachrichten",
        "Benachrichtigungen für private Nachrichten",
        Key.key("paper"),
        SettingCategory.CHAT,
    ),
    SettingKeys.CHAT_PINGS to SettingKeyDisplay(
        "Chat Pings",
        "Benachrichtigung, sobald dich jemand erwähnt",
        Key.key("bell"),
        SettingCategory.CHAT,
    ),
    SettingKeys.CLAN_CHAT_MESSAGES to SettingKeyDisplay(
        "Clan-Chat",
        "Zeigt Nachrichten aus dem Clan-Chat an",
        Key.key("writable_book"),
        SettingCategory.CLAN,
    ),
    SettingKeys.CLAN_INVITES to SettingKeyDisplay(
        "Clan-Einladungen",
        "Benachrichtigungen für Einladungen zu Clans",
        Key.key("shield"),
        SettingCategory.CLAN,
    ),
    SettingKeys.FRIEND_NOTIFICATIONS to SettingKeyDisplay(
        "Freunde-Benachrichtigungen",
        "Benachrichtigungen über Aktivitäten deiner Freunde",
        Key.key("heart_of_the_sea"),
        SettingCategory.FRIENDS,
    ),
    SettingKeys.FRIEND_REQUESTS to SettingKeyDisplay(
        "Freundschaftsanfragen annehmen",
        "Erlaubt anderen Spielern, dir Freundschaftsanfragen zu senden",
        Key.key("emerald"),
        SettingCategory.FRIENDS,
    ),
    SettingKeys.FRIEND_REQUEST_NOTIFICATIONS to SettingKeyDisplay(
        "Freundschaftsanfrage-Benachrichtigungen",
        "Benachrichtigungen für neue Freundschaftsanfragen",
        Key.key("player_head"),
        SettingCategory.FRIENDS,
    ),
    SettingKeys.FRIEND_SOUNDS to SettingKeyDisplay(
        "Freunde-Sounds",
        "Spielt Sounds bei Benachrichtigungen von Freunden ab",
        Key.key("amethyst_shard"),
        SettingCategory.FRIENDS,
    ),
    SettingKeys.LOBBY_SCROLL_SOUND to SettingKeyDisplay(
        "Scroll-Sound",
        "Spielt einen Sound beim Scrollen in der Lobby ab",
        Key.key("note_block"),
        SettingCategory.LOBBY,
    ),
    SettingKeys.SHOW_NAMETAGS to SettingKeyDisplay(
        "Nametags anzeigen",
        "Zeigt die Nametags anderer Spieler an",
        Key.key("name_tag"),
        SettingCategory.GENERAL,
    ),
    SettingKeys.AUTO_QUEUE_AFTER_RECONNECT to SettingKeyDisplay(
        "Automatische Rückkehr",
        "Bringt dich nach einem kurzen Verbindungsabbruch automatisch zurück auf deinen vorherigen Server",
        Key.key("recovery_compass"),
        SettingCategory.GENERAL,
    ),
).mapKeys { it.key.name }
