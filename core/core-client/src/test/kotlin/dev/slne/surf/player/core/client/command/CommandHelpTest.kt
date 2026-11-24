package dev.slne.surf.player.core.client.command

import dev.slne.surf.player.core.client.chat.command.ChatCommands
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CommandHelpTest {

    @Test
    fun `clicking a help line suggests the command up to its first argument`() {
        assertEquals("/friend add ", CommandHelp.suggestionOf("/friend add <spieler>"))
        assertEquals("/friend list ", CommandHelp.suggestionOf("/friend list [seite]"))
        assertEquals("/clan leave", CommandHelp.suggestionOf("/clan leave"))
    }

    @Test
    fun `admin usage joins root, feature, action and arguments`() {
        val help = AdminActionHelp("enqueue", "<spieler> [server]", "", "surf.player.admin.queue.enqueue")
        assertEquals("/surfplayer queue enqueue <spieler> [server]", help.usage("surfplayer", "queue"))

        val noArguments = AdminActionHelp("reload", "", "", "surf.player.admin.queue.reload")
        assertEquals("/surfplayervelocity queue reload", noArguments.usage("surfplayervelocity", "queue"))
    }

    @Test
    fun `toggle accepts german and english words`() {
        assertEquals(true, Toggle.parse("an"))
        assertEquals(false, Toggle.parse("AUS"))
        assertEquals(true, Toggle.parse("on"))
        assertEquals(null, Toggle.parse("vielleicht"))
    }

    @Test
    fun `a lookup query string is split into its filters`() {
        val query = ChatCommands.parseLookupQuery("--sender Notch --message \"Hallo Welt\" --range 1d")

        assertEquals("Notch", query["--sender"])
        assertEquals("\"Hallo Welt\"", query["--message"])
        assertEquals("1d", query["--range"])
        assertTrue(ChatCommands.lookupFilterKeys.containsAll(query.keys))
    }
}
