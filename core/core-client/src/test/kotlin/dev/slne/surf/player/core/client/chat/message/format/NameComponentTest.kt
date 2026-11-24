package dev.slne.surf.player.core.client.chat.message.format

import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.plain
import net.kyori.adventure.text.event.ClickEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.util.*

class NameComponentTest {

    @Test
    fun `clicking a name suggests a private message`() {
        val click = nameComponent("Keviro", "").clickEvent()

        assertEquals(ClickEvent.Action.SUGGEST_COMMAND, click?.action())
        assertEquals(ClickEvent.suggestCommand("/msg Keviro "), click)
    }

    @Test
    fun `without the teleport permission there is no teleport button`() {
        val name = buildText { appendName("Keviro", UUID.randomUUID(), "", showTeleportButton = false) }

        assertEquals("Keviro", name.plain())
        assertFalse(name.plain().contains("TP"))
    }
}
