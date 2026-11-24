package dev.slne.surf.social.core.client.contentcreator.command

import dev.slne.surf.social.api.contentcreator.platform.PlatformState
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class LiveStateDebugCommandTest {

    @Test
    fun `reports the new state`() {
        assertEquals(
            "Der Live-Status wurde erfolgreich auf ONLINE gesetzt.",
            LiveStateDebugCommand.stateChangedMessage(PlatformState.ONLINE)
        )
        assertEquals(
            "Der Live-Status wurde erfolgreich auf OFFLINE gesetzt.",
            LiveStateDebugCommand.stateChangedMessage(PlatformState.OFFLINE)
        )
    }
}
