package dev.slne.surf.player.core.client.friends.command

import dev.slne.surf.api.core.messages.adventure.plain
import dev.slne.surf.player.core.client.command.CommandOutcome
import dev.slne.surf.player.core.client.command.failure
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*

class FriendAdminCommandsTest {

    @Test
    fun `nothing deleted is a failure naming the player and target`() {
        assertEquals(
            failure("NotAmmo hat keine Freundschaftsanfrage-Cooldowns."),
            FriendAdminCommands.cooldownsCleared(0, "NotAmmo", null)
        )
        assertEquals(
            failure("NotAmmo hat keine Freundschaftsanfrage-Cooldowns gegenüber FPNS."),
            FriendAdminCommands.cooldownsCleared(0, "NotAmmo", "FPNS")
        )
    }

    @Test
    fun `deleted cooldowns are counted`() {
        val one = FriendAdminCommands.cooldownsCleared(1, "NotAmmo", "FPNS") as CommandOutcome.Reply
        val many = FriendAdminCommands.cooldownsCleared(3, "NotAmmo", null) as CommandOutcome.Reply

        assertTrue(one.message.plain().endsWith("Ein Freundschaftsanfrage-Cooldown von NotAmmo gegenüber FPNS wurde gelöscht."))
        assertTrue(many.message.plain().endsWith("3 Freundschaftsanfrage-Cooldowns von NotAmmo wurden gelöscht."))
    }

    @Test
    fun `a player has no cooldown towards themself`() {
        val player = UUID.randomUUID()

        val outcome = runBlocking { FriendAdminCommands.clearCooldowns(player, "NotAmmo", player, "NotAmmo") }

        assertTrue(outcome is CommandOutcome.Failure)
    }
}
