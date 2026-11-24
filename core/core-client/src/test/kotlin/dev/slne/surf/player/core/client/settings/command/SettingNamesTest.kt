package dev.slne.surf.player.core.client.settings.command

import dev.slne.surf.player.api.settings.setting.SettingKeys
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class SettingNamesTest {
    private val names = listOf(
        SettingKeys.CHAT_PINGS.name,
        SettingKeys.DIRECT_MESSAGES.name,
        SettingKeys.CLAN_CHAT_MESSAGES.name,
        SettingKeys.FRIEND_REQUESTS.name,
        SettingKeys.CLAN_INVITES.name,
    )

    @Test
    fun `setting keys are namespaced`() {
        assertEquals("chat:pings", SettingKeys.CHAT_PINGS.name)
    }

    @Test
    fun `a namespaced key resolves to itself`() {
        assertEquals("chat:pings", SettingNames.resolve("chat:pings", names))
        assertEquals("friend:requests", SettingNames.resolve("friend:requests", names))
    }

    @Test
    fun `matching ignores case and surrounding whitespace`() {
        assertEquals("chat:pings", SettingNames.resolve("Chat:PINGS", names))
        assertEquals("chat:direct-messages", SettingNames.resolve("  chat:direct-messages ", names))
    }

    @Test
    fun `a name without namespace resolves when it is unique`() {
        assertEquals("chat:pings", SettingNames.resolve("pings", names))
        assertEquals("clan:invites", SettingNames.resolve("INVITES", names))
    }

    @Test
    fun `an ambiguous name without namespace does not resolve`() {
        assertNull(SettingNames.resolve("chat-messages", names + "team:chat-messages"))
    }

    @Test
    fun `unknown and empty input does not resolve`() {
        assertNull(SettingNames.resolve("chat:unknown", names))
        assertNull(SettingNames.resolve("minecraft:pings", names))
        assertNull(SettingNames.resolve("", names))
        assertNull(SettingNames.resolve("chat", names))
    }

    @Test
    fun `every registered key is suggested for empty input, sorted`() {
        assertEquals(names.sorted(), SettingNames.suggestions("", names))
    }

    @Test
    fun `suggestions match the full key or the name without namespace, ignoring case`() {
        assertEquals(listOf("chat:direct-messages", "chat:pings"), SettingNames.suggestions("CHAT:", names))
        assertEquals(listOf("chat:pings"), SettingNames.suggestions("pi", names))
        assertEquals(listOf("clan:chat-messages"), SettingNames.suggestions("clan:chat", names))
    }
}
