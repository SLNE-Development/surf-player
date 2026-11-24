package dev.slne.surf.player.core.client.command

import dev.slne.surf.api.core.messages.adventure.plain
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NoticeTest {

    @Test
    fun `a notice is a reply, not a failure`() {
        val outcome = notice("Es sind keine Streak-Pausen eingetragen.")

        assertTrue(outcome is CommandOutcome.Reply, outcome.toString())
        assertTrue(
            (outcome as CommandOutcome.Reply).message.plain().endsWith("Es sind keine Streak-Pausen eingetragen."),
            outcome.message.plain(),
        )
    }

    @Test
    fun `a notice uses the info prefix`() {
        val notice = noticeMessage("Die Warteschlange lobby ist leer.")
        assertEquals(noticeMessage("Die Warteschlange lobby ist leer."), notice)
        assertTrue(notice.plain().length > "Die Warteschlange lobby ist leer.".length, notice.plain())
    }
}
