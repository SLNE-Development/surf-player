package dev.slne.surf.player.core.client.contentcreator.command

import dev.slne.surf.player.api.connection.impl.TwitchConnection
import dev.slne.surf.player.api.contentcreator.platform.PlatformState
import dev.slne.surf.player.api.contentcreator.platform.PlatformType
import dev.slne.surf.player.core.client.contentcreator.service.ContentCreatorService
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.audience.Audience
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LiveStateDebugCommandTest {
    private val player = UUID.randomUUID()

    @AfterEach
    fun clear() {
        ContentCreatorService.clear()
    }

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

    @Test
    fun `a link made while online is read again before the state is set`() = runBlocking {
        ContentCreatorService.beginSession(player)
        var loads = 0

        LiveStateDebugCommand.setTwitchState(Audience.empty(), player, PlatformState.ONLINE) { uuid ->
            loads++
            ContentCreatorService.cacheCreator(uuid, TwitchConnection("devadmintest", 123L))
        }

        assertEquals(1, loads)
        val platform = ContentCreatorService.getContentCreator(player)?.getStreamingPlatform(PlatformType.TWITCH)
        assertEquals(PlatformState.ONLINE, platform?.state)
    }

    @Test
    fun `a cached creator is not read again`() = runBlocking {
        ContentCreatorService.beginSession(player)
        ContentCreatorService.cacheCreator(player, TwitchConnection("devadmintest", 123L))
        var loads = 0

        LiveStateDebugCommand.setTwitchState(Audience.empty(), player, PlatformState.ONLINE) { loads++ }

        assertEquals(0, loads)
    }

    @Test
    fun `a player without a link stays unconnected`() = runBlocking {
        ContentCreatorService.beginSession(player)

        LiveStateDebugCommand.setTwitchState(Audience.empty(), player, PlatformState.ONLINE) { uuid ->
            ContentCreatorService.cacheCreator(uuid, null)
        }

        assertNull(ContentCreatorService.getContentCreator(player))
    }
}
